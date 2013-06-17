
package me.zeus.MassBlockChanger;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_5_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_5_R3.ChunkSection;



public class MassBlockChanger
{
	
	
	/**
	 * 
	 * Sets the specified block(s) as fast as possible utilizing NMS (net.minecraft.sever) code
	 * 
	 * @param b - Block object
	 * @param typeId - The id of the material you want it changed to
	 * @param data - data value (default 0)
	 * @param autoUpdateChunks - Update chunks automatically?
	 * @param printTime - Print amount of time taken
	 */
	public static void setBlockSuperFast(Block b, int typeId, byte data, boolean autoUpdateChunks, boolean printTime)
	{
		List<Chunk> affectedChunks = new ArrayList<Chunk>();
		Chunk c = b.getChunk();
		affectedChunks.add(c);
		net.minecraft.server.v1_5_R3.Chunk chunk = ((org.bukkit.craftbukkit.v1_5_R3.CraftChunk) c).getHandle();
		long time1 = System.currentTimeMillis();
		try
		{
			Field f = chunk.getClass().getDeclaredField("sections");
			f.setAccessible(true);
			ChunkSection[] sections = (ChunkSection[]) f.get(chunk);
			ChunkSection chunksection = sections[b.getY() >> 4];
			
			if (chunksection == null)
			{
				if (typeId == 0)
					return;
				chunksection = sections[b.getY() >> 4] = new ChunkSection(b.getY() >> 4 << 4, !chunk.world.worldProvider.f);
			}
			
			chunksection.setTypeId(b.getX() & 15, b.getY() & 15, b.getZ() & 15, typeId);
			chunksection.setData(b.getX() & 15, b.getY() & 15, b.getZ() & 15, data);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (autoUpdateChunks)
			sendClientChanges(b.getLocation(), affectedChunks);
		if (printTime)
		{
			System.out.println("Time taken: " + (System.currentTimeMillis() - time1) + " milliseconds");
		}
	}
	
	
	
	/**
	 * 
	 * @param blocks - Block objects
	 * @param typeId - The id of the material you want it changed to
	 * @param data - data value (default 0)
	 * @param autoUpdateChunks - Update chunks automatically?
	 * @param printTime - Print amount of time taken
	 */
	public static void setBlocksSuperFast(List<Block> blocks, int typeId, byte data, boolean autoUpdateChunks, boolean printTime)
	{
		long time1 = System.currentTimeMillis();
		for (Block b : blocks)
			setBlockSuperFast(b, typeId, data, autoUpdateChunks, false);
		if (printTime)
		{
			System.out.println("Time taken: " + (System.currentTimeMillis() - time1) + " milliseconds");
		}
	}
	
	
	
	public static void sendClientChanges(Location center, List<Chunk> chunks)
	{
		int threshold = (Bukkit.getServer().getViewDistance() << 4) + 32;
		threshold = threshold * threshold;
		
		List<net.minecraft.server.v1_5_R3.ChunkCoordIntPair> pairs = new ArrayList<net.minecraft.server.v1_5_R3.ChunkCoordIntPair>();
		for (Chunk c : chunks)
		{
			pairs.add(new ChunkCoordIntPair(c.getX(), c.getZ()));
		}
		
		for (Player player : center.getWorld().getPlayers())
		{
			int px = player.getLocation().getBlockX();
			int pz = player.getLocation().getBlockZ();
			if ((px - center.getX()) * (px - center.getX()) + (pz - center.getZ()) * (pz - center.getZ()) < threshold)
			{
				queueChunks(((CraftPlayer) player).getHandle(), pairs);
			}
		}
	}
	
	
	
	@SuppressWarnings("unchecked")
	private static void queueChunks(net.minecraft.server.v1_5_R3.EntityPlayer ep, List<net.minecraft.server.v1_5_R3.ChunkCoordIntPair> pairs)
	{
		Set<net.minecraft.server.v1_5_R3.ChunkCoordIntPair> queued = new HashSet<net.minecraft.server.v1_5_R3.ChunkCoordIntPair>();
		for (Object o : ep.chunkCoordIntPairQueue)
		{
			queued.add((net.minecraft.server.v1_5_R3.ChunkCoordIntPair) o);
		}
		for (net.minecraft.server.v1_5_R3.ChunkCoordIntPair pair : pairs)
		{
			if (!queued.contains(pair))
			{
				ep.chunkCoordIntPairQueue.add(pair);
			}
		}
	}
}
