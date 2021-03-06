package com.samuel.spectrite.entities;

import com.google.common.collect.Sets;
import com.samuel.spectrite.Spectrite;
import com.samuel.spectrite.helpers.SpectriteHelper;
import com.samuel.spectrite.init.ModPotions;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Set;

public class EntitySpectriteTippedArrow extends EntityArrow {
	
	private PotionType potionType = PotionTypes.EMPTY;
	private Potion potion;
    private final Set<PotionEffect> customPotionEffects = Sets.<PotionEffect>newHashSet();
	
	public EntitySpectriteTippedArrow(EntityTippedArrow arrow, PotionType potionType) {
		super(arrow.getEntityWorld(), arrow.shootingEntity != null ? (EntityLivingBase) arrow.shootingEntity : null);
		float velocity = Math.min(arrow.getEntityWorld().rand.nextFloat() + 0.25f, 1.0f);
		if (this.shootingEntity != null) {
			this.shoot(shootingEntity, shootingEntity.rotationPitch, shootingEntity.rotationYaw, 0.0F, velocity * 3.0F, 1.0F);
		}
		if (velocity == 1.0f) {
			this.setIsCritical(true);
		}
		this.setPotionEffect(potionType);
	}

	public EntitySpectriteTippedArrow(World worldIn) {
		super(worldIn);
	}

	public void setPotionEffect(PotionType potionType)
    {
        this.potionType = potionType;
        if (!potionType.getEffects().isEmpty()) {
	        PotionEffect potionEffect = potionType.getEffects().get(0);
	    	this.potion = potionEffect.getPotion();
	    	this.customPotionEffects.add(potionEffect);
    	}
    }

    public void addEffect(PotionEffect effect)
    {
        this.customPotionEffects.add(effect);
    }

    @Override
	protected void entityInit()
    {
        super.entityInit();
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
	public void onUpdate()
    {
        super.onUpdate();

        if (this.world.isRemote)
        {
            if (this.inGround)
            {
                if (this.timeInGround % 5 == 0)
                {
                    this.spawnPotionParticles(1);
                }
            }
            else
            {
                this.spawnPotionParticles(2);
            }
        }
        else if (this.inGround && this.timeInGround != 0 && !this.customPotionEffects.isEmpty() && this.timeInGround >= 600)
        {
            this.world.setEntityState(this, (byte)0);
            this.potionType = PotionTypes.EMPTY;
            this.customPotionEffects.clear();
        }
    }

    private void spawnPotionParticles(int particleCount)
    {
    	int offsetLevel = this.potionType == PotionTypes.EMPTY || ModPotions.SPECTRITE.equals(potion) ? 0 : ModPotions.SPECTRITE_DAMAGE.equals(this.potion) ? 1 : 2;
        int i = SpectriteHelper.getCurrentSpectriteColour(offsetLevel);

        if (particleCount > 0)
        {
            double d0 = (i >> 16 & 255) / 255.0D;
            double d1 = (i >> 8 & 255) / 255.0D;
            double d2 = (i >> 0 & 255) / 255.0D;

            for (int j = 0; j < particleCount; ++j)
            {
            	Spectrite.Proxy.spawnSpectriteSpellParticle(this.world, this.posX + (this.rand.nextDouble() - 0.5D) * this.width,
        			this.posY + this.rand.nextDouble() * this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * this.width, d0, d1, d2, offsetLevel);
            }
        }
    }
    
    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);

        if (this.potionType != PotionTypes.EMPTY && this.potionType != null)
        {
            compound.setString("Potion", PotionType.REGISTRY.getNameForObject(this.potionType).toString());
        }

        if (!this.customPotionEffects.isEmpty())
        {
            NBTTagList nbttaglist = new NBTTagList();

            for (PotionEffect potioneffect : this.customPotionEffects)
            {
                nbttaglist.appendTag(potioneffect.writeCustomPotionEffectToNBT(new NBTTagCompound()));
            }

            compound.setTag("CustomPotionEffects", nbttaglist);
        }
    }

    
    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);

        if (compound.hasKey("Potion", 8))
        {
            this.potionType = PotionUtils.getPotionTypeFromNBT(compound);
        }

        for (PotionEffect potioneffect : PotionUtils.getFullEffectsFromTag(compound))
        {
            this.addEffect(potioneffect);
        }
    }

    @Override
    protected void arrowHit(EntityLivingBase living)
    {
        super.arrowHit(living);

        for (PotionEffect potioneffect : this.potionType.getEffects())
        {
            living.addPotionEffect(new PotionEffect(potioneffect.getPotion(), Math.max(potioneffect.getDuration() / 8, 1), potioneffect.getAmplifier(), potioneffect.getIsAmbient(), potioneffect.doesShowParticles()));
        }

        if (!this.customPotionEffects.isEmpty())
        {
            for (PotionEffect potioneffect1 : this.customPotionEffects)
            {
                living.addPotionEffect(potioneffect1);
            }
        }
    }

    @Override
    protected ItemStack getArrowStack()
    {
        if (this.customPotionEffects.isEmpty() && this.potionType == PotionTypes.EMPTY)
        {
            return new ItemStack(Items.ARROW);
        }
        else
        {
            ItemStack itemstack = new ItemStack(Items.TIPPED_ARROW);
            PotionUtils.addPotionToItemStack(itemstack, this.potionType);
            PotionUtils.appendEffects(itemstack, this.customPotionEffects);
            
            return itemstack;
        }
    }
   
    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id)
    {
        if (id == 0)
        {
        	int offsetLevel = this.potionType == PotionTypes.EMPTY || ModPotions.SPECTRITE.equals(this.potion) ? 0 : ModPotions.SPECTRITE_DAMAGE.equals(this.potion) ? 1 : 2;
            int i = SpectriteHelper.getCurrentSpectriteColour(offsetLevel);

            if (i != -1)
            {
                double d0 = (i >> 16 & 255) / 255.0D;
                double d1 = (i >> 8 & 255) / 255.0D;
                double d2 = (i >> 0 & 255) / 255.0D;

                for (int j = 0; j < 20; ++j)
                {
                	Spectrite.Proxy.spawnSpectriteSpellParticle(this.world,  this.posX + (this.rand.nextDouble() - 0.5D) * this.width,
            			this.posY + this.rand.nextDouble() * this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * this.width, d0, d1, d2, offsetLevel);
                }
            }
        }
        else
        {
            super.handleStatusUpdate(id);
        }
    }
}
