package theorbtwo.minecraft.visualSound;

import net.minecraft.client.Minecraft;

public class SoundRecord {
	public String text;
	public float length;
	public float start_time;
	public float x;
	public float y;
	public float z;

	public SoundRecord(String in_text, float in_length, float in_x, float in_y, float in_z) {
		this.text = in_text;
		// In units of ticks = 1/20 second?
		this.length = in_length;
		this.start_time = Minecraft.getMinecraft().theWorld.getWorldTime();
		this.x = in_x;
		this.y = in_y;
		this.z = in_z;
	}
}
