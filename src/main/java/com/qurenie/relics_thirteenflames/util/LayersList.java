package com.qurenie.relics_thirteenflames.util;

import net.minecraft.util.Mth;
import org.zeith.hammeranims.api.animation.interp.BlendMode;
import org.zeith.hammeranims.api.animation.interp.Query;
import org.zeith.hammeranims.api.animsys.layer.AnimationLayer;
import org.zeith.hammeranims.api.animsys.layer.ILayerMask;

import java.util.ArrayList;
import java.util.Optional;

public class LayersList
{
	ArrayList<LayerDefinition> layers = new ArrayList<>();
	
	public void addFirst(LayerDefinition LayerDefinition)
	{
		layers.add(0, LayerDefinition);
	}
	
	public void addFirst(String name, BlendMode blendType, float weight)
	{
		layers.add(0, new LayerDefinition(name, blendType, weight));
	}
	
	public boolean addAfter(String after, LayerDefinition LayerDefinition)
	{
		Optional<LayerDefinition> ll = layers.stream().filter(l -> l.name().equals(after)).findFirst();
		if(ll.isPresent())
		{
			layers.add(layers.indexOf(ll.get()) + 1, LayerDefinition);
			return true;
		} else
		{
			return false;
		}
	}
	
	public boolean addAfter(String after, String name, BlendMode blendType, float weight)
	{
		Optional<LayerDefinition> ll = layers.stream().filter(l -> l.name().equals(after)).findFirst();
		if(ll.isPresent())
		{
			layers.add(layers.indexOf(ll.get()) + 1, new LayerDefinition(name, blendType, weight));
			return true;
		} else
		{
			return false;
		}
	}
	
	public boolean addBefore(String before, LayerDefinition LayerDefinition)
	{
		Optional<LayerDefinition> ll = layers.stream().filter(l -> l.name().equals(before)).findFirst();
		if(ll.isPresent())
		{
			layers.add(layers.indexOf(ll.get()) + 1, LayerDefinition);
			return true;
		} else
		{
			return false;
		}
	}
	
	public boolean addBefore(String before, String name, BlendMode blendType, float weight)
	{
		Optional<LayerDefinition> ll = layers.stream().filter(l -> l.name().equals(before)).findFirst();
		if(ll.isPresent())
		{
			layers.add(layers.indexOf(ll.get()), new LayerDefinition(name, blendType, weight));
			return true;
		} else
		{
			return false;
		}
	}
	
	public void addLast(LayerDefinition LayerDefinition)
	{
		layers.add(LayerDefinition);
	}
	
	public void addLast(String name, BlendMode blendType, float weight)
	{
		layers.add(new LayerDefinition(name, blendType, weight));
	}
	
	public ArrayList<LayerDefinition> getLayers()
	{
		return layers;
	}
	
	public static final class LayerDefinition
			extends AnimationLayer.Builder
	{
		public LayerDefinition(String name, BlendMode blendType, float weight)
		{
			super(name);
			this.blendMode(blendType);
			this.weight = Mth.clamp(weight, 0, 1);
		}
		
		public String name()
		{
			return name;
		}
		
		public LayerDefinition(String name)
		{
			super(name);
		}
		
		@Override
		public LayerDefinition query(Query query)
		{
			return (LayerDefinition) super.query(query);
		}
		
		@Override
		public LayerDefinition mask(ILayerMask mask)
		{
			return (LayerDefinition) super.mask(mask);
		}
		
		@Override
		public LayerDefinition weight(float weight)
		{
			return (LayerDefinition) super.weight(weight);
		}
		
		@Override
		public LayerDefinition blendMode(BlendMode blendMode)
		{
			return (LayerDefinition) super.blendMode(blendMode);
		}
		
		@Override
		public LayerDefinition allowAutoSync(boolean allowAutoSync)
		{
			return (LayerDefinition) super.allowAutoSync(allowAutoSync);
		}
		
		@Override
		public LayerDefinition preventAutoSync()
		{
			return (LayerDefinition) super.preventAutoSync();
		}
		
		@Override
		public LayerDefinition persistent(boolean persistent)
		{
			return (LayerDefinition) super.persistent(persistent);
		}
		
		@Override
		public LayerDefinition nonPersistent()
		{
			return (LayerDefinition) super.nonPersistent();
		}
	}
}