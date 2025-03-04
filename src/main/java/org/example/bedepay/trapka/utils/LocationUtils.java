package org.example.bedepay.trapka.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Утилитарный класс для работы с локациями
 */
public class LocationUtils {

    /**
     * Получает список всех блоков в кубе
     * @param center центр куба
     * @param size размер куба
     * @return список всех блоков в кубе
     */
    public static List<Block> getBlocksInCube(Location center, int size) {
        List<Block> blocks = new ArrayList<>();
        World world = center.getWorld();
        
        int halfSize = size / 2;
        
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int y = -halfSize; y <= halfSize; y++) {
                for (int z = -halfSize; z <= halfSize; z++) {
                    blocks.add(world.getBlockAt(
                            center.getBlockX() + x,
                            center.getBlockY() + y,
                            center.getBlockZ() + z
                    ));
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * Получает список блоков на поверхности куба (только внешний слой)
     * @param center центр куба
     * @param size размер куба
     * @return список блоков на поверхности куба
     */
    public static List<Block> getCubeShell(Location center, int size) {
        List<Block> blocks = new ArrayList<>();
        World world = center.getWorld();
        
        int halfSize = size / 2;
        
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int y = -halfSize; y <= halfSize; y++) {
                for (int z = -halfSize; z <= halfSize; z++) {
                    // Добавляем только блоки на поверхности куба
                    if (Math.abs(x) == halfSize || Math.abs(y) == halfSize || Math.abs(z) == halfSize) {
                        blocks.add(world.getBlockAt(
                                center.getBlockX() + x,
                                center.getBlockY() + y,
                                center.getBlockZ() + z
                        ));
                    }
                }
            }
        }
        
        return blocks;
    }
    
    /**
     * Создает платформу в указанной локации
     * @param center центр платформы
     * @param size размер платформы
     * @param material материал платформы
     * @return список измененных блоков
     */
    public static List<Block> createPlatform(Location center, int size, Material material) {
        List<Block> changedBlocks = new ArrayList<>();
        World world = center.getWorld();
        
        int halfSize = size / 2;
        
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                Block block = world.getBlockAt(
                        center.getBlockX() + x,
                        center.getBlockY() - 1, // Размещаем платформу под ногами
                        center.getBlockZ() + z
                );
                
                // Сохраняем блок и меняем его на материал платформы
                changedBlocks.add(block);
                block.setType(material);
            }
        }
        
        return changedBlocks;
    }
    
    /**
     * Проверяет, безопасна ли локация для телепортации
     * @param location локация для проверки
     * @return true, если локация безопасна
     */
    public static boolean isSafeLocation(Location location) {
        Block block = location.getBlock();
        Block blockAbove = location.clone().add(0, 1, 0).getBlock();
        Block blockBelow = location.clone().add(0, -1, 0).getBlock();
        
        // Проверяем, что блок и блок над ним - воздух, а блок под игроком твердый
        return !block.getType().isSolid() &&
                !blockAbove.getType().isSolid() &&
                blockBelow.getType().isSolid();
    }
} 