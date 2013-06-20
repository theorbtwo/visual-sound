/**
 *
 */
package theorbtwo.minecraft.visualSound;

import java.util.ArrayList;
import java.util.EnumSet;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author theorbtwo
 */
@SideOnly(Side.CLIENT)
public class RenderTickHandler implements ITickHandler {

  /* (non-Javadoc)
   * @see cpw.mods.fml.common.ITickHandler#tickStart(java.util.EnumSet, java.lang.Object[])
   */
  @Override
  public void tickEnd(EnumSet<TickType> type, Object... tickData) {
    /* type will be [RENDER], since that's the only tick type we asked for.  tickData is a double between 0 and 1 (?), the partial tick time? */

    Minecraft mc = Minecraft.getMinecraft();
    mc.mcProfiler.startSection("VisualSound");

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

    ArrayList<SoundRecord> soundRecords = VisualSound.soundRecords;
    for (SoundRecord r : soundRecords) {
      // Skip sounds that started before now -- this can happen when time rolls over, or when we change dimension.
      if (time < r.start_time + r.length && r.start_time < time) {
        // Right.  Now we need to figure out where on the screen this sound goes, given r.[xyz].
        // Remember, minecraft uses a somewhat odd coordinate system.
        // +x is east
        // +y is upward
        // +z is south
        double distance = player.getDistance(r.x, r.y, r.z);
        double screen_x;
        double screen_y = mc_height/2;
        //  alpha = 0 -> transparent, 1 -> opaque
        double alpha;

        if (distance == 0) {
          screen_x = mc_width/2;
          screen_y = mc_height/2;
          alpha = 1;
        } else {
          // player positioning
          double px = player.posX;
          double py = player.posY + player.getEyeHeight();
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

          //FMLLog.info("rrh = %f deg", rrh);
          //screen_x = rrh;                 // -180 ~ 180
          //screen_x += 180;                //    0 ~ 360
          //screen_x /= 360;                //    0 ~ 1
          //screen_x *= mc_width;           //    0 ~ width
          screen_x = ((rrh + 180)/360)*mc_width;

          // Right, now do the annoying bits over again, for the vertical angle.

          //  90 = down at feet
          //   0 = straight ahead
          // -90 = up at sky
          double pv = player.rotationPitch;

          // rv -- the angle of the vector from the player to the event, in world degrees.
          double rv = (180/Math.PI) * Math.atan2(ry, rx);
          //FMLLog.info("rv = %f", rv);
          rv = rv % 360;
          if (rv > 180) {
            rv -= 180;
          }

          // rv, relative to the direction the player is looking.
          double rrv = rv - pv;
          if (rrv > 180) {
            rrv -= 360;
          }

          screen_y = rrv;                 //  -90 ~ 90
          //FMLLog.info("screen_y = %f", screen_y);
          screen_y += 90;                 //    0 ~ 180
          //FMLLog.info("screen_y = %f", screen_y);
          screen_y /= 180;                //    0 ~ 1
          //FMLLog.info("screen_y = %f", screen_y);
          screen_y *= mc_height;          //    0 ~ width
          //FMLLog.info("screen_y = %f", screen_y);
          //screen_y = ((rrv + 180)/360)*mc_height;


          //screen_x = 0.5 * mc_height;
          FMLLog.info("distance=%f", distance);
          FMLLog.info("volume=%f", r.volume);
          // + 0.001 just so that distance of zero gives arbitrary big number instead of error.
          double power = r.volume / (distance+0.001);
          FMLLog.info("power=%f", power);

          alpha = power * 10;
        }

        drawString(String.format("%s (d=%1.4f, v=%1.4f, a=%1.4f)", r.text, distance, r.volume, alpha), (int)screen_x, (int)screen_y, alpha);
      }
    }

    mc.mcProfiler.endSection();
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

  public static void drawString(String s, int x, int y, double alpha) {
    //FMLLog.info("drawString: %s, %d, %d", s, x, y);
    //drawString(s, x, y, Colour.WHITE);
    Minecraft mc = Minecraft.getMinecraft();
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    int r = 0xFF;
    int g = 0xFF;
    int b = 0xFF;
    int a = (int)(alpha*0xff);
    if (a > 0xFF)
      a = 0xff;

    // 0xaarrggbb
    int color = (a << 24) | (r << 16) | (g << 8) | b;

    mc.fontRenderer.drawString(s, x, y, color, true);
  }

}
