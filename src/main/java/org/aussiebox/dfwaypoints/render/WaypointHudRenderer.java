package org.aussiebox.dfwaypoints.render;

import dev.dfonline.flint.Flint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.aussiebox.dfwaypoints.DFWaypoints;
import org.aussiebox.dfwaypoints.waypoints.Waypoint;
import org.aussiebox.dfwaypoints.waypoints.Waypoints;

public class WaypointHudRenderer {

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int height = MinecraftClient.getInstance().getWindow().getScaledHeight();
        int textY = height/2+10;

        if (!Waypoints.waypointsLookingAt.isEmpty()) {
            OrderedText text = Text.translatable("tip.dfwaypoints.lookwarp", DFWaypoints.lookwarp.getBoundKeyLocalizedText()).asOrderedText();
            int x = width/2-(textRenderer.getWidth(text)/2);
            int y = height/2-17;

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
        }
        for (Waypoint waypoint : Waypoints.waypointsLookingAt.keySet()) {
            OrderedText name = Text.literal(waypoint.getName()).asOrderedText();
            int x = width/2-(textRenderer.getWidth(name)/2);

            if (Flint.getUser().getPlot() == null) return;
            if (!Waypoints.getWaypointsOfType(Flint.getUser().getPlot().getId(), waypoint.getType()).contains(waypoint))
                Waypoints.waypointsLookingAt.removeDouble(waypoint);

            context.drawText(textRenderer, name, x+1, textY+1, waypoint.textOutlineColor, false);
            context.drawText(textRenderer, name, x-1, textY+1, waypoint.textOutlineColor, false);
            context.drawText(textRenderer, name, x+1, textY-1, waypoint.textOutlineColor, false);
            context.drawText(textRenderer, name, x-1, textY-1, waypoint.textOutlineColor, false);
            context.drawText(textRenderer, name, x+1, textY, waypoint.textOutlineColor, false);
            context.drawText(textRenderer, name, x-1, textY, waypoint.textOutlineColor, false);
            context.drawText(textRenderer, name, x, textY+1, waypoint.textOutlineColor, false);
            context.drawText(textRenderer, name, x, textY-1, waypoint.textOutlineColor, false);

            context.drawText(
                    textRenderer,
                    name,
                    x,
                    textY,
                    waypoint.textColor,
                    false
            );
            textY += textRenderer.fontHeight + 3;
        }
    }
}