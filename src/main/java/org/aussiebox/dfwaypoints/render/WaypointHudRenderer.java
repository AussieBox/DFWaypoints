package org.aussiebox.dfwaypoints.render;

import dev.dfonline.flint.Flint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.aussiebox.dfwaypoints.DFWaypoints;
import org.aussiebox.dfwaypoints.config.DFWConfig;
import org.aussiebox.dfwaypoints.waypoints.Waypoint;
import org.aussiebox.dfwaypoints.waypoints.Waypoints;

public class WaypointHudRenderer {

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int height = MinecraftClient.getInstance().getWindow().getScaledHeight();
        int textY = height/2+10;
        if (DFWConfig.waypointHudAlignment.asString().contains("top"))
            textY = 3;
        else if (DFWConfig.waypointHudAlignment.asString().contains("bottom")) {
            textY = height-3-textRenderer.fontHeight;
            if (DFWConfig.waypointHudAlignment == DFWConfig.WaypointHudAlignment.BOTTOM_CENTER)
                textY -= 21;
        }
        else if (DFWConfig.waypointHudAlignment != DFWConfig.WaypointHudAlignment.MIDDLE_CENTER) {
            textY = height/2-((textRenderer.fontHeight+3)*(Waypoints.waypointsLookingAt.size()+1)/2);
            if (!DFWConfig.showLookwarpTip) textY = height/2-((textRenderer.fontHeight+3)*(Waypoints.waypointsLookingAt.size())/2);
        }


        if (!Waypoints.waypointsLookingAt.isEmpty() && DFWConfig.showLookwarpTip) {
            OrderedText text = Text.translatable("tip.dfwaypoints.lookwarp", DFWaypoints.lookwarp.getBoundKeyLocalizedText()).asOrderedText();
            int x = width/2-(textRenderer.getWidth(text)/2);
            if (DFWConfig.waypointHudAlignment.asString().contains("left"))
                x = 3;
            if (DFWConfig.waypointHudAlignment.asString().contains("right"))
                x = width-3-textRenderer.getWidth(text);
            int y = height/2-17;
            if (DFWConfig.waypointHudAlignment != DFWConfig.WaypointHudAlignment.MIDDLE_CENTER) y = textY;

            context.drawText(textRenderer, text, x+1, y+1, 0xFF032620, false);
            context.drawText(textRenderer, text, x-1, y+1, 0xFF032620, false);
            context.drawText(textRenderer, text, x+1, y-1, 0xFF032620, false);
            context.drawText(textRenderer, text, x-1, y-1, 0xFF032620, false);
            context.drawText(textRenderer, text, x+1, y, 0xFF032620, false);
            context.drawText(textRenderer, text, x-1, y, 0xFF032620, false);
            context.drawText(textRenderer, text, x, y+1, 0xFF032620, false);
            context.drawText(textRenderer, text, x, y-1, 0xFF032620, false);

            context.drawText(
                    textRenderer,
                    text,
                    x,
                    y,
                    0xFF8CF4E2,
                    false
            );
            if (DFWConfig.waypointHudAlignment != DFWConfig.WaypointHudAlignment.MIDDLE_CENTER) {
                if (!DFWConfig.waypointHudAlignment.asString().contains("bottom")) textY += 3+textRenderer.fontHeight;
                else textY -= 3+textRenderer.fontHeight;
            }
        }
        for (Waypoint waypoint : Waypoints.waypointsLookingAt.keySet()) {
            OrderedText name = Text.literal(waypoint.getName()).asOrderedText();
            int x = width/2-(textRenderer.getWidth(name)/2);
            if (DFWConfig.waypointHudAlignment.asString().contains("left"))
                x = 3;
            if (DFWConfig.waypointHudAlignment.asString().contains("right"))
                x = width-3-textRenderer.getWidth(name);

            if (Flint.getUser().getPlot() == null) return;
            if (!Waypoints.getWaypointsOfType(Flint.getUser().getPlot().getId(), waypoint.getType()).contains(waypoint))
                Waypoints.waypointsLookingAt.removeDouble(waypoint);

            context.drawText(textRenderer, name, x+1, textY+1, waypoint.textOutlineColor.getRGB(), false);
            context.drawText(textRenderer, name, x-1, textY+1, waypoint.textOutlineColor.getRGB(), false);
            context.drawText(textRenderer, name, x+1, textY-1, waypoint.textOutlineColor.getRGB(), false);
            context.drawText(textRenderer, name, x-1, textY-1, waypoint.textOutlineColor.getRGB(), false);
            context.drawText(textRenderer, name, x+1, textY, waypoint.textOutlineColor.getRGB(), false);
            context.drawText(textRenderer, name, x-1, textY, waypoint.textOutlineColor.getRGB(), false);
            context.drawText(textRenderer, name, x, textY+1, waypoint.textOutlineColor.getRGB(), false);
            context.drawText(textRenderer, name, x, textY-1, waypoint.textOutlineColor.getRGB(), false);

            context.drawText(
                    textRenderer,
                    name,
                    x,
                    textY,
                    waypoint.textColor.getRGB(),
                    false
            );
            if (!DFWConfig.waypointHudAlignment.asString().contains("bottom")) textY += 3+textRenderer.fontHeight;
            else textY -= 3+textRenderer.fontHeight;
        }
    }
}