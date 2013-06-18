/**
 *
 */
package theorbtwo.minecraft.visualSound;

import java.util.ArrayList;
import java.util.EnumSet;

import org.lwjgl.opengl.GL11;

import com.google.common.math.DoubleMath;

import net.machinemuse.general.geometry.Colour;
import net.machinemuse.utils.render.MuseRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author theorb
 *
 * Based on https://github.com/MachineMuse/MachineMusePowersuits/blob/master/src/minecraft/net/machinemuse/powersuits/tick/RenderTickHandler.java
 */
@SideOnly(Side.CLIENT)
public class RenderTickHandler implements ITickHandler {

	/* (non-Javadoc)
	 * @see cpw.mods.fml.common.ITickHandler#tickStart(java.util.EnumSet, java.lang.Object[])
	 */
	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP player = mc.thePlayer;
        if (player == null) {
        	// We aren't in the actual game yet.
        	return;
        }

        float time = mc.theWorld.getWorldTime();

        ScaledResolution res = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        int mc_width = res.getScaledWidth();
        int mc_height = res.getScaledHeight();

        /*
		player.addChatMessage(String.format("tickStart called, type=%s, tickData=%s",
				type == null ? "null" : type,
				tickData == null ? "null" : array_to_string(tickData)));
         */
        /* type will be [RENDER], since that's the only tick type we asked for.  tickData is a double between 0 and 1 (?), the partial tick time? */

        ArrayList<SoundRecord> soundRecords = VisualSound.soundRecords;
        for (SoundRecord r : soundRecords) {
        	if (time < r.start_time + r.length) {
        		// Right.  Now we need to figure out where on the screen this sound goes, given r.[xyz].
        		// Remember, minecraft uses a somewhat odd coordinate system.
        		// +x is east
        		// +y is upward
        		// +z is south
        		double distance = player.getDistance(r.x, r.y, r.z);
        		// for the purposes of screen_x and screen_y, (0, 0) is the center of the screen, with +x being right, and +y being down.
        		double screen_x;
        		double screen_y = mc_height/2;

        		if (distance == 0) {
        			screen_x = mc_width/2;
        			screen_y = mc_height/2;
        		} else {
        			// player.rotationYawHead: what direction n/w/s/e we're looking -- same as debug screen f.
        			//    0 = south
        			//   90 = west
        			//  180 = north
        			// -180 = north
        			//  -90 = east

        			// player positioning
        			double px = player.posX;
        			double py = player.posY;
        			double pz = player.posZ;
        			// player's horizontal angle -- matches convention on f3 screen.
        			double ph = (double)player.rotationYawHead % (double)360;
        			if (ph > 180) {
        				ph -= 360;
        			}

        			// event positioning
        			double ex = r.x;
        			double ey = r.y;
        			double ez = r.z;

        			// relative positioning
        			double rx = px - ex;
        			double ry = py - ey;
        			double rz = pz - ez;

        			// rh -- the angle of the vector from the player to the event, in world degrees.
        			// I'm not clear on why this +90 is needed.
        			double rh = (180/Math.PI) * Math.atan2(rz, rx) + 90;
        			rh = rh % 360;
        			if (rh > 180) {
        				rh -= 360;
        			}

        			// rh, relative to the direction the player is facing.
        			double rrh = rh - ph;

        			rrh = rrh % 360;
        			if (rrh > 180) {
        				rrh -= 360;
        			}
        			//FMLLog.info("ph - rh: %f", ph - rh);

        			FMLLog.info("rrh = %f deg", rrh);
        			//screen_x = rrh;                 // -180 ~ 180
        			//screen_x += 180;                //    0 ~ 360
        			//screen_x /= 360;                //    0 ~ 1
        			//screen_x *= mc_width;           //    0 ~ width
        			screen_x = ((rrh + 180)/360)*mc_width;

        			
        			//screen_x = 0.5 * mc_height;
        		}

        		drawString(r.text, (int)screen_x, (int)screen_y);
        	}
        }
	}

	public static CharSequence array_to_string(Object[] array) {
		StringBuilder sb = new StringBuilder();

		sb.append("[");
		for (Object e : array) {
			sb.append(e);
			sb.append(", ");
		}
		sb.append("]");

		return sb;
	}

	/* (non-Javadoc)
	 * @see cpw.mods.fml.common.ITickHandler#tickEnd(java.util.EnumSet, java.lang.Object[])
	 */
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		// This happens before *anything* is rendered, so whatever gets rendered here will be overwritten.
	}

	/* (non-Javadoc)
	 * @see cpw.mods.fml.common.ITickHandler#ticks()
	 */
	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.RENDER);
	}

	/* (non-Javadoc)
	 * @see cpw.mods.fml.common.ITickHandler#getLabel()
	 */
	@Override
	public String getLabel() {
		return "visual sound render_tick";
	}

    public static void drawString(String s, int x, int y) {
    	FMLLog.info("drawString: %s, %d, %d", s, x, y);
        //drawString(s, x, y, Colour.WHITE);
        Minecraft mc = Minecraft.getMinecraft();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        mc.fontRenderer.drawString(s, x, y, 0xFFFFFF, true);
    }

}
