package me.playground.data;

import org.apache.commons.lang3.SerializationUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import me.playground.utils.Utils;

import java.io.Serializable;

public class CustomPersistentDataType {
	
	public static final PersistentDataType<byte[], ItemStack[]> ITEMSTACK_ARRAYLIST = new ItemStackArrayDataType();
	public static final PersistentDataType<byte[], Vector> VECTOR = new VectorDataType();
	
	private static class ItemStackArrayDataType implements PersistentDataType<byte[], ItemStack[]> {
		@Override
		public @NotNull Class<byte[]> getPrimitiveType() {
			return byte[].class;
		}

		@Override
		public @NotNull Class<ItemStack[]> getComplexType() {
			return ItemStack[].class;
		}

		@Override
		public byte @NotNull [] toPrimitive(@NotNull ItemStack @NotNull [] complex, @NotNull PersistentDataAdapterContext context) {
			return SerializationUtils.serialize(Utils.itemStackArrayToBase64(complex));
		}

		@Override
		public @NotNull ItemStack @NotNull [] fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
			return Utils.itemStackArrayFromBase64(SerializationUtils.deserialize(primitive));
		}
	}

	private static class VectorDataType implements PersistentDataType<byte[], Vector> {
		@Override
		public @NotNull Class<byte[]> getPrimitiveType() {
			return byte[].class;
		}

		@Override
		public @NotNull Class<Vector> getComplexType() {
			return Vector.class;
		}

		@Override
		public byte @NotNull [] toPrimitive(@NotNull Vector complex, @NotNull PersistentDataAdapterContext context) {
			return SerializationUtils.serialize((Serializable) complex.serialize());
		}

		@Override
		public @NotNull Vector fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
			return Vector.deserialize(SerializationUtils.deserialize(primitive));
		}
	}
}
