package com.dungeonchaos.dungeonchaos.service;

import com.dungeonchaos.dungeonchaos.exception.InformationNotFoundException;
import com.dungeonchaos.dungeonchaos.model.Inventory;
import com.dungeonchaos.dungeonchaos.model.InventoryItem;
import com.dungeonchaos.dungeonchaos.model.Item.Item;
import com.dungeonchaos.dungeonchaos.repository.InventoryItemRepository;
import com.dungeonchaos.dungeonchaos.repository.InventoryRepository;
import com.dungeonchaos.dungeonchaos.repository.ItemRepository;
import com.dungeonchaos.dungeonchaos.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InventoryService {
    private InventoryRepository inventoryRepository;
    private ItemRepository itemRepository;
    private InventoryItemRepository inventoryItemRepository;
    private PlayerRepository playerRepository;

    @Autowired
    InventoryService(InventoryRepository inventoryRepository, ItemRepository itemRepository, InventoryItemRepository inventoryItemRepository, PlayerRepository playerRepository) {
        this.inventoryRepository = inventoryRepository;
        this.itemRepository = itemRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.playerRepository = playerRepository;
    }

    public Inventory getInventoryById(Long inventoryId) {
        return inventoryRepository.findById(inventoryId).orElseThrow(() -> new InformationNotFoundException("Inventory is not found with id " + inventoryId));
    }

    public Inventory addItemToInventory(Long inventoryId, Long itemId) {
        Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow(() -> new InformationNotFoundException("Inventory is not found with id " + inventoryId));
        Optional<InventoryItem> inventoryItem = inventoryItemRepository.findByInventory_IdAndItem_Id(inventoryId, itemId);
        if (inventoryItem.isPresent()) {
            inventoryItem.get().increaseItemQuantityByOne();
        } else {
            Item item = itemRepository.findById(itemId).orElseThrow(() -> new InformationNotFoundException("Item is not found with id " + itemId));
            InventoryItem newInventoryItem = new InventoryItem(inventory, item);
            inventoryItemRepository.save(newInventoryItem);
            inventory.getInventoryItems().add(newInventoryItem);
        }
        return inventoryRepository.save(inventory);
    }

    public Inventory removeItemFromInventory(Long inventoryId, Long itemId) {
        Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow(() -> new InformationNotFoundException("Inventory is not found with id " + inventoryId));
        InventoryItem inventoryItem = inventoryItemRepository.findByInventory_IdAndItem_Id(inventoryId, itemId).orElseThrow(() -> new InformationNotFoundException("Item is not found with id " + itemId));
        inventoryItem.decreaseItemQuantityByOne();
        if (inventoryItem.getItemQuantity() <= 0) {
            inventoryItemRepository.delete(inventoryItem);
            inventory.removeInventoryItem(inventoryItem);
        }
        ;
        return inventoryRepository.save(inventory);
    }
}
