package org.aussiebox.dfwaypoints.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.dfonline.flint.Flint;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.MappableRingBuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.aussiebox.dfwaypoints.DFWaypoints;
import org.aussiebox.dfwaypoints.waypoints.Waypoint;
import org.aussiebox.dfwaypoints.waypoints.WaypointType;
import org.aussiebox.dfwaypoints.waypoints.Waypoints;
import org.joml.*;
import org.lwjgl.system.MemoryUtil;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class WaypointRenderPipeline implements ClientModInitializer {
    private static WaypointRenderPipeline instance;

    private static final RenderPipeline WAYPOINT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of(DFWaypoints.MOD_ID, "pipeline/waypoint"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build()
    );

    private static BufferBuilder buffer;
    private static final BufferAllocator allocator = new BufferAllocator(RenderLayer.field_64009);
    private static MappableRingBuffer vertexBuffer;
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    @Override
    public void onInitializeClient() {
        instance = this;
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(WaypointRenderPipeline::extractAndDrawWaypoint);
    }

    public static WaypointRenderPipeline getInstance() {
        return instance;
    }

    public static boolean isPlayerLookingAtPosition(Vec3d position, int checkRange, int checkDistance) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return false;

        Vec3d pos = player.getCameraPosVec(1.0F);
        Vec3d lookDir = player.getHeadRotationVector();
        for (int i = 0; i < checkDistance*10; i++) {
            pos = pos.add(lookDir.multiply(0.1));
            if (pos.isWithinRangeOf(position, checkRange, checkRange)) return true;
        }

        return false;
    }

    public static void extractAndDrawWaypoint(WorldRenderContext context) {
        renderWaypoint(context);
        drawWaypoint(MinecraftClient.getInstance(), WAYPOINT);
        renderWaypointLine(context);
        drawWaypoint(MinecraftClient.getInstance(), RenderPipelines.LINES);
    }

    private static void renderWaypoint(WorldRenderContext context) {
        MatrixStack matrices = context.matrices();
        CameraRenderState cameraRenderState = context.worldState().cameraRenderState;
        Vec3d cameraPos = cameraRenderState.pos;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        if (Flint.getUser().getPlot() != null) {
            Map<WaypointType, Waypoint[]> waypoints = Waypoints.getWaypoints(Flint.getUser().getPlot().getId());

            for (Waypoint[] waypointList : waypoints.values()) {
                for (Waypoint waypoint : waypointList) {
                    if (!waypoint.render) {
                        Waypoints.waypointsLookingAt.removeDouble(waypoint);
                        continue;
                    }

                    matrices.push();
                    matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

                    if (buffer == null) {
                        buffer = new BufferBuilder(allocator, WAYPOINT.getVertexFormatMode(), WAYPOINT.getVertexFormat());
                    }

                    Vec3d pos = waypoint.getPosition();
                    double distance = player.getEyePos().distanceTo(pos);

                    int alpha = 255;
                    if (distance <= 3 && distance >= 1.2) alpha = (int) ((distance-1.2)*255/2);
                    if (distance < 1.2) alpha = 0;
                    if (distance > 10) {
                        boolean looking = isPlayerLookingAtPosition(pos, 3, 100);
                        if (looking) Waypoints.waypointsLookingAt.put(waypoint, distance);
                        else Waypoints.waypointsLookingAt.removeDouble(waypoint);
                    } else Waypoints.waypointsLookingAt.removeDouble(waypoint);

                    Quaternionf quaternion = cameraRenderState.orientation;
                    matrices.multiply(quaternion, (float) pos.x, (float) pos.y, (float) pos.z);

                    renderWaypointContent(matrices.peek().getPositionMatrix(), buffer, (float) pos.x, (float) pos.y, (float) pos.z, ColorHelper.withAlpha(alpha, waypoint.waypointColor.getRGB()));

                    matrices.pop();

                    OrderedText waypointName = Text.of(waypoint.getName()).asOrderedText();

                    matrices.push();
                    matrices.translate(pos.x-cameraPos.x, pos.y-cameraPos.y, pos.z-cameraPos.z);
                    matrices.multiply(quaternion);
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
                    if (Waypoints.waypointsLookingAt.containsKey(waypoint)) matrices.scale(0.1F, 0.1F, -0.1F);
                    else matrices.scale(0.02F, 0.02F, -0.02F);

                    context.commandQueue().submitText(
                            matrices,
                            -((float) MinecraftClient.getInstance().textRenderer.getWidth(waypointName)/2),
                            Waypoints.waypointsLookingAt.containsKey(waypoint) ? 8 : 20,
                            waypointName,
                            true,
                            TextRenderer.TextLayerType.SEE_THROUGH,
                            15,
                            ColorHelper.withAlpha(alpha, waypoint.textColor.getRGB()),
                            0x00000000,
                            ColorHelper.withAlpha(alpha, waypoint.textOutlineColor.getRGB())
                    );

                    matrices.pop();
                }
            }
        }
    }

    private static void renderWaypointContent(Matrix4fc positionMatrix, BufferBuilder buffer, float x, float y, float z, int argb) {
        buffer.vertex(positionMatrix, (float) (x-0.25), (float) (y-0.25), z).color(argb);
        buffer.vertex(positionMatrix, (float) (x+0.25), (float) (y-0.25), z).color(argb);
        buffer.vertex(positionMatrix, (float) (x+0.25), (float) (y+0.25), z).color(argb);
        buffer.vertex(positionMatrix, (float) (x-0.25), (float) (y+0.25), z).color(argb);
    }

    private static void renderWaypointLine(WorldRenderContext context) {
        MatrixStack matrices = context.matrices();
        CameraRenderState cameraRenderState = context.worldState().cameraRenderState;
        Vec3d cameraPos = cameraRenderState.pos;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        if (Flint.getUser().getPlot() != null) {
            for (Waypoint waypoint : Waypoints.waypointsLookingAt.keySet()) {
                if (buffer == null) {
                    buffer = new BufferBuilder(allocator, RenderPipelines.LINES.getVertexFormatMode(), RenderPipelines.LINES.getVertexFormat());
                }

                matrices.push();
                matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

                Vec3d pos = waypoint.getPosition();
                renderWaypointLineContent(matrices.peek().getPositionMatrix(), buffer, (float) pos.x, (float) pos.y, (float) pos.z, ColorHelper.withAlpha(100, waypoint.waypointColor.getRGB()));

                matrices.pop();
            }
        }
    }

    private static void renderWaypointLineContent(Matrix4fc positionMatrix, BufferBuilder buffer, float x, float y, float z, int argb) {
        if (MinecraftClient.getInstance().player == null) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        RenderTickCounter counter = MinecraftClient.getInstance().getRenderTickCounter();

        float tickDelta = counter.getTickProgress(true);
        float playerX = (float) (player.lastX + (player.getX() - player.lastX) * tickDelta);
        float playerY = (float) (player.lastY + (player.getY() - player.lastY) * tickDelta + 1);
        float playerZ = (float) (player.lastZ + (player.getZ() - player.lastZ) * tickDelta);

        buffer.vertex(positionMatrix, playerX, playerY, playerZ).normal(1.0F, 1.0F, 1.0F).lineWidth(4.0F).color(argb);
        buffer.vertex(positionMatrix, x, y, z).normal(1.0F, 1.0F, 1.0F).lineWidth(4.0F).color(argb);
    }

    private static void drawWaypoint(MinecraftClient client, @SuppressWarnings("SameParameterValue") RenderPipeline pipeline) {
        if (Flint.getUser().getPlot() == null) return;
        if (Waypoints.getWaypoints(Flint.getUser().getPlot().getId()).isEmpty()) return;
        if (buffer == null) return;

        // Build the buffer
        BuiltBuffer builtBuffer = buffer.end();
        BuiltBuffer.DrawParameters drawParameters = builtBuffer.getDrawParameters();
        VertexFormat format = drawParameters.format();

        GpuBuffer vertices = upload(drawParameters, format, builtBuffer);

        draw(client, pipeline, builtBuffer, drawParameters, vertices, format);

        // Rotate the vertex buffer so we are less likely to use buffers that the GPU is using
        vertexBuffer.rotate();
        buffer = null;
    }

    private static GpuBuffer upload(BuiltBuffer.DrawParameters drawParameters, VertexFormat format, BuiltBuffer builtBuffer) {
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }

            vertexBuffer = new MappableRingBuffer(() -> DFWaypoints.MOD_ID + " waypoint render pipeline", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        }

        // Copy vertex data into the vertex buffer
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.getBlocking().slice(0, builtBuffer.getBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.getBuffer(), mappedView.data());
        }

        return vertexBuffer.getBlocking();
    }

    private static void draw(MinecraftClient client, RenderPipeline pipeline, BuiltBuffer builtBuffer, BuiltBuffer.DrawParameters drawParameters, GpuBuffer vertices, VertexFormat format) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        if (pipeline.getVertexFormatMode() == VertexFormat.DrawMode.QUADS) {
            // Sort the quads if there is translucency
            builtBuffer.sortQuads(allocator, RenderSystem.getProjectionType().getVertexSorter());
            // Upload the index buffer
            indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.getSortedBuffer());
            indexType = builtBuffer.getDrawParameters().indexType();
        } else {
            // Use the general shape index buffer for non-quad draw modes
            RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = shapeIndexBuffer.getIndexBuffer(drawParameters.indexCount());
            indexType = shapeIndexBuffer.getIndexType();
        }

        // Actually execute the draw
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .write(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> DFWaypoints.MOD_ID + " waypoint render pipeline rendering", client.getFramebuffer().getColorAttachmentView(), OptionalInt.empty(), client.getFramebuffer().getDepthAttachmentView(), OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            // Bind texture if applicable:
            // Sampler0 is used for texture inputs in vertices
            // renderPass.bindTexture("Sampler0", textureSetup.texure0(), textureSetup.sampler0());

            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);

            // The base vertex is the starting index when we copied the data into the vertex buffer divided by vertex size
            //noinspection ConstantValue
            renderPass.drawIndexed(0 / format.getVertexSize(), 0, drawParameters.indexCount(), 1);
        }

        builtBuffer.close();
    }

    public void close() {
        allocator.close();

        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }
}
