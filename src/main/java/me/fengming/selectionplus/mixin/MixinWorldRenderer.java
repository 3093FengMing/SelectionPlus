package me.fengming.selectionplus.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.fengming.selectionplus.SelectionPlus;
import me.fengming.selectionplus.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.OptionalDouble;

import static me.fengming.selectionplus.SelectionPlus.config;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Shadow private ClientWorld world;

    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Inject(method = "drawBlockOutline", at = @At("HEAD"))
    private void drawBlockOutline_proxy(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        Config.Lines l = config.lines;
        Config.Sides s = config.sides;
        Config.Setting t = config.setting;
        Config.Blink b = config.blink;
        if (t.mode.equalsIgnoreCase("none")) {
            return;
        }
        VertexConsumerProvider.Immediate immediate = this.bufferBuilders.getEntityVertexConsumers();

        RenderLayer.MultiPhaseParameters.Builder builder = RenderLayer.MultiPhaseParameters.builder();
        builder.lineWidth(new RenderPhase.LineWidth(l.thickness == 0 ? OptionalDouble.empty() : OptionalDouble.of(l.thickness)));
        builder.layering(new RenderPhase.Layering("view_offset_z_layering", () -> {
                    RenderSystem.pushMatrix();
                    RenderSystem.scalef(0.99975586F, 0.99975586F, 0.99975586F);
                }, RenderSystem::popMatrix));
        builder.transparency(new RenderPhase.Transparency("translucent_transparency", () -> {
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
                }, () -> {
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
        }));
        builder.target(new RenderPhase.Target("item_entity_target", () -> {
                    if (MinecraftClient.isFabulousGraphicsOrBetter()) {
                        MinecraftClient.getInstance().worldRenderer.getEntityFramebuffer().beginWrite(false);
                    }}, () -> {
                    if (MinecraftClient.isFabulousGraphicsOrBetter()) {
                        MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
                    }}));
        builder.writeMaskState(new RenderPhase.WriteMaskState(true, true));

        RenderLayer line = RenderLayer.of("line2", VertexFormats.POSITION_COLOR, 1, 256, builder.build(false));
        VertexConsumer vertexConsumer3 = immediate.getBuffer(line);
        VertexConsumer vertexConsumer4 = Tessellator.getInstance().getBuffer();

        if (b.enable) {
            if (SelectionPlus.timer.tick <= 0) {
                SelectionPlus.timer.reverse = false;
                SelectionPlus.timer.reset();
            }
            if (SelectionPlus.timer.tick <= b.speed) {
                if (l.rainbow) {
                    Color color = SelectionPlus.rainbow.getColor();
                    renderLine(matrices, vertexConsumer3, entity, d, e, f,
                            blockPos, blockState, color.getRed(), color.getGreen(), color.getBlue(),
                            (l.a/255.0F-b.alpha/255.0F)*(b.speed-SelectionPlus.timer.tick)/b.speed, t);
//                    renderSide(matrices, vertexConsumer4, entity, d, e, f,
//                            blockPos, blockState, color.getRed(), color.getGreen(), color.getBlue(),
//                            (l.a/255.0F-b.alpha/255.0F)*(b.speed-SelectionPlus.timer.tick)/b.speed, t);
                    SelectionPlus.rainbow.next();
                } else {
                    renderLine(matrices, vertexConsumer3, entity, d, e, f,
                            blockPos, blockState, l.r, l.g, l.b,
                            (l.a/255.0F-b.alpha/255.0F)*(b.speed-SelectionPlus.timer.tick)/b.speed, t);
//                    renderSide(matrices, vertexConsumer4, entity, d, e, f,
//                            blockPos, blockState, l.r, l.g, l.b,
//                            (l.a/255.0F-b.alpha/255.0F)*(b.speed-SelectionPlus.timer.tick)/b.speed, t);
                }
            } else {
                SelectionPlus.timer.tick = 21;
                SelectionPlus.timer.reverse = true;
            }
        } else {
            SelectionPlus.timer.reset();
            if (l.rainbow) {
                Color color = SelectionPlus.rainbow.getColor();
                renderLine(matrices, vertexConsumer3, entity, d, e, f,
                        blockPos, blockState, color.getRed(), color.getGreen(), color.getBlue(), l.a/255.0F, t);
//                renderSide(matrices, vertexConsumer4, entity, d, e, f,
//                        blockPos, blockState, color.getRed(), color.getGreen(), color.getBlue(), l.a/255.0F, t);
                SelectionPlus.rainbow.next();
            } else {
                renderLine(matrices, vertexConsumer3, entity, d, e, f,
                        blockPos, blockState, l.r, l.g, l.b, l.a/255.0F, t);
//                renderSide(matrices, vertexConsumer4, entity, d, e, f,
//                        blockPos, blockState, l.r, l.g, l.b, l.a/255.0F, t);
            }
        }
    }

    private void renderLine(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState, float r, float g, float b, float a, Config.Setting t) {
        if (t.mode.equalsIgnoreCase("lines") || t.mode.equalsIgnoreCase("both") ) {
            drawShapeOutline(matrices, vertexConsumer,
                    blockState.getOutlineShape(this.world, blockPos, ShapeContext.of(entity)),
                    (double) blockPos.getX() - d, (double) blockPos.getY() - e,
                    (double) blockPos.getZ() - f, r/255.0F, g/255.0F, b/255.0F, a);
        }
    }

//    private void renderSide(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos blockPos, BlockState blockState, float r, float g, float b, float a, Config.Setting t) {
//        if (t.mode.equalsIgnoreCase("sides") || t.mode.equalsIgnoreCase("both") ) {
//            drawShapeSide(matrices, vertexConsumer,
//                    blockState.getVisualShape(this.world, blockPos, ShapeContext.of(entity)),
//                    (double) blockPos.getX() - d, (double) blockPos.getY() - e,
//                    (double) blockPos.getZ() - f, r/255.0F, g/255.0F, b/255.0F, a);
//        }
//    }

    private static void drawShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
        Matrix4f matrix4f = matrices.peek().getModel();
        voxelShape.forEachBox((k, l, m, n, o, p) -> {
            vertexConsumer.vertex(matrix4f, (float)(k + d), (float)(l + e), (float)(m + f)).color(g, h, i, j).next();
            vertexConsumer.vertex(matrix4f, (float)(n + d), (float)(o + e), (float)(p + f)).color(g, h, i, j).next();
        });
    }


//    private static void drawShapeSide(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
//        Matrix4f matrix4f = matrices.peek().getModel();
//        voxelShape.forEachEdge((k, l, m, n, o, p) -> {
//            vertexConsumer.vertex(matrix4f, (float)(k + d), (float)(l + e), (float)(m + f)).color(g, h, i, j).next();
//            vertexConsumer.vertex(matrix4f, (float)(n + d), (float)(o + e), (float)(p + f)).color(g, h, i, j).next();
//        });
//    }

}
