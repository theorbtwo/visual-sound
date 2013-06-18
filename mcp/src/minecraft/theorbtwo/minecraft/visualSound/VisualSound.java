/**
 *
 */
package theorbtwo.minecraft.visualSound;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import com.google.common.eventbus.EventBus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid="VisualSound", name="VisualSound", version="0.0.0")
@NetworkMod(clientSideRequired=false, serverSideRequired=false)

/**
 * @author theorb
 *
 */
public class VisualSound {

    // The instance of your mod that Forge uses.
    @Instance("Generic")
    public static VisualSound instance;

	public static ArrayList<SoundRecord> soundRecords = new ArrayList<SoundRecord>();

    @PreInit
    public void preInit(FMLPreInitializationEvent event) {
            MinecraftForge.EVENT_BUS.register(this);
            TickRegistry.registerTickHandler(new RenderTickHandler(), Side.CLIENT);
    }

    @ForgeSubscribe
    public void playSoundEvent(PlaySoundEvent event) {
    	Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
    	mc.mcProfiler.startSection("VisualSound");

    	soundRecords.add(new SoundRecord(event.name, 20, event.x, event.y, event.z));

		mc.mcProfiler.endSection();
    }


    @ForgeSubscribe
    // net.minecraftforge.client.event.RenderGameOverlayEvent.RenderGameOverlayEvent(RenderGameOverlayEvent, ElementType)
    // See net.minecraft.client.renderer.tileentity.TileEntitySignRenderer.renderTileEntitySignAt(TileEntitySign, double, double, double, float) for how to render?
    public void renderGameOverlayEvent(RenderGameOverlayEvent event) {

    	// GLU.gluProject(event_x, event_y, event_z, modelMatrix, projMatrix, viewport, win_pos);
    }



    /*
    @Init
    public void load(FMLInitializationEvent event) {
            proxy.registerRenderers();
    }

    @PostInit
    public void postInit(FMLPostInitializationEvent event) {
            // Stub Method
    }
    */

}
