package com.qurenie.relics_thirteenflames.content.entities.data;

import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.zeith.hammerlib.api.fml.ICustomRegistrar;
import org.zeith.hammerlib.util.java.Cast;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface EntityDataSerializerHL<T>
		extends EntityDataSerializer<T>, ICustomRegistrar
{
	@Override
	default void performRegister(RegisterEvent event, ResourceLocation id)
	{
		event.register(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, id, Cast.constant(this));
	}
	
	static <T> EntityDataSerializerHL<T> simple(final FriendlyByteBuf.Writer<T> writer, final FriendlyByteBuf.Reader<T> reader)
	{
		return new ForValueTypeHL<>()
		{
			@Override
			public void write(FriendlyByteBuf buf, T obj)
			{
				writer.accept(buf, obj);
			}
			
			@Override
			public T read(FriendlyByteBuf obj)
			{
				return reader.apply(obj);
			}
		};
	}
	
	static <T> EntityDataSerializerHL<T> simpleCopyable(final FriendlyByteBuf.Writer<T> writer, final FriendlyByteBuf.Reader<T> reader, UnaryOperator<T> copy)
	{
		return new ForValueTypeHL<>()
		{
			@Override
			public void write(FriendlyByteBuf buf, T obj)
			{
				writer.accept(buf, obj);
			}
			
			@Override
			public T read(FriendlyByteBuf obj)
			{
				return reader.apply(obj);
			}
			
			@Override
			public T copy(T original)
			{
				return copy.apply(original);
			}
		};
	}
	
	static <T> EntityDataSerializer<Optional<T>> optional(FriendlyByteBuf.Writer<T> writer, FriendlyByteBuf.Reader<T> reader)
	{
		return simple(writer.asOptional(), reader.asOptional());
	}
	
	static <T extends Enum<T>> EntityDataSerializer<T> simpleEnum(Class<T> eClass)
	{
		return simple(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(eClass));
	}
	
	static <T> EntityDataSerializer<T> simpleId(IdMap<T> idmap)
	{
		return simple((buf, t) -> buf.writeId(idmap, t), buf -> buf.readById(idmap));
	}
	
	interface ForValueTypeHL<T>
			extends EntityDataSerializerHL<T>
	{
		default T copy(T original)
		{
			return original;
		}
	}
}