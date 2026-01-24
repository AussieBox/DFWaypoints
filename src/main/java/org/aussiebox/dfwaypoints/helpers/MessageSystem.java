package org.aussiebox.dfwaypoints.helpers;

import net.minecraft.sound.SoundEvents;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.aussiebox.dfwaypoints.DFWaypoints;

public class MessageSystem {

    // Success Color: 0x8CF4E2
    // Info Color: 0xABE5FF
    // Error Color: 0xEB3535

    public static void SuccessMessage(MutableText message, Boolean playSound) {

        Text resultMessage = Text.empty()
                .append(
                        Text.literal("g ")
                                .styled(
                                        style -> style.withFont(new StyleSpriteSource.Font(Identifier.of("dfwaypoints:chat")))
                                )
                                .styled(
                                        style -> style.withHoverEvent(
                                                new HoverEvent.ShowText(
                                                        Text.literal("DFWaypoints Mark ")
                                                                .withColor(0xFFFFFF)
                                                                .append(
                                                                        Text.literal("(Task Success)")
                                                                                .withColor(0x8CF4E2)
                                                                )
                                                )
                                        )
                                )
                )
                .append(
                        message
                                .withColor(0x8CF4E2)
                                .styled(
                                        style -> style.withFont(new StyleSpriteSource.Font(Identifier.of("minecraft:default")))
                                )
                );


        assert DFWaypoints.MC.player != null;
        DFWaypoints.MC.player.sendMessage(resultMessage, false);
        if(playSound = true) {
            DFWaypoints.MC.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
        }

    }

    public static void InfoMessage(MutableText message, Boolean playSound) {

        Text resultMessage = Text.empty()
                .append(
                        Text.literal("b ")
                                .styled(
                                        style -> style.withFont(new StyleSpriteSource.Font(Identifier.of("dfwaypoints:chat")))
                                )
                                .styled(
                                        style -> style.withHoverEvent(
                                                new HoverEvent.ShowText(
                                                        Text.literal("DFWaypoints Mark ")
                                                                .withColor(0xFFFFFF)
                                                                .append(
                                                                        Text.literal("(Task Info)")
                                                                                .withColor(0xABE5FF)
                                                                )
                                                )
                                        )
                                )
                )
                .append(
                        message
                                .withColor(0xABE5FF)
                                .styled(
                                        style -> style.withFont(new StyleSpriteSource.Font(Identifier.of("minecraft:default")))
                                )
                );


        assert DFWaypoints.MC.player != null;
        DFWaypoints.MC.player.sendMessage(resultMessage, false);
        if(playSound = true) {
            DFWaypoints.MC.player.playSound(SoundEvents.ITEM_TRIDENT_RETURN);
        }

    }

    public static void ErrorMessage(MutableText message, Boolean playSound) {

        Text resultMessage = Text.empty()
                .append(
                        Text.literal("r ")
                                .styled(
                                        style -> style.withFont(new StyleSpriteSource.Font(Identifier.of("dfwaypoints:chat")))
                                )
                                .styled(
                                        style -> style.withHoverEvent(
                                                new HoverEvent.ShowText(
                                                        Text.literal("DFWaypoints Mark ")
                                                                .withColor(0xFFFFFF)
                                                                .append(
                                                                        Text.literal("(Task Error)")
                                                                                .withColor(0xFF6868)
                                                                )
                                                )
                                        )
                                )
                )
                .append(
                        message
                                .withColor(0xFF6868)
                                .styled(
                                        style -> style.withFont(new StyleSpriteSource.Font(Identifier.of("minecraft:default")))
                                )
                );


        assert DFWaypoints.MC.player != null;
        DFWaypoints.MC.player.sendMessage(resultMessage, false);
        if(playSound = true) {
            DFWaypoints.MC.player.playSound(SoundEvents.ENTITY_SHULKER_HURT_CLOSED);
        }

    }

}