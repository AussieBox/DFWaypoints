package org.aussiebox.dfwaypoints.render;

import dev.dfonline.flint.Flint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.aussiebox.dfwaypoints.waypoints.Waypoint;
import org.aussiebox.dfwaypoints.waypoints.Waypoints;

public class WaypointHudRenderer {

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        TextRenderer textRenderer =MinecraftClient.getInstance().textRenderer;
        int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int height = MinecraftClient.getInstance().getWindow().getScaledHeight();
        int textY = height/2+10;

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