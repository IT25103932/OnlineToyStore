package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.Supplier;
import com.OnlineToyStore.Sllit.service.InventoryService;
import com.OnlineToyStore.Sllit.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class InventoryController {

    @Autowired
    private InventoryService inventoryService; // handles stock logic

    @Autowired
    private SupplierService supplierService;   // handles supplier CRUD

    // ── GET /inventory — main inventory overview page ─────────────────────────
    // optional ?filter=low / ?filter=out / ?filter=healthy
    @GetMapping("/inventory")
    public String inventoryOverview(
            @RequestParam(required = false) String filter,
            Model model) {

        // Send summary stats to the HTML page
        model.addAttribute("totalToys",      inventoryService.getTotalToyCount());
        model.addAttribute("lowStockCount",  inventoryService.getLowStockCount());
        model.addAttribute("outOfStockCount",inventoryService.getOutOfStockCount());
        model.addAttribute("totalUnits",     inventoryService.getTotalStockUnits());

        // Send the correct filtered list based on the ?filter= value
        if ("low".equals(filter)) {
            model.addAttribute("toys", inventoryService.getLowStockToys());
            model.addAttribute("filterLabel", "Low Stock");

        } else if ("out".equals(filter)) {
            model.addAttribute("toys", inventoryService.getOutOfStockToys());
            model.addAttribute("filterLabel", "Out of Stock");

        } else if ("healthy".equals(filter)) {
            model.addAttribute("toys", inventoryService.getHealthyStockToys());
            model.addAttribute("filterLabel", "Healthy Stock");

        } else {
            model.addAttribute("toys", inventoryService.getAllInventory());
            model.addAttribute("filterLabel", "All Toys");
        }

        model.addAttribute("currentFilter", filter);
        return "inventory/list"; // loads templates/inventory/list.html
    }

    // ── POST /inventory/restock — increase a toy's stock quantity ─────────────
    @PostMapping("/inventory/restock")
    public String restockToy(
            @RequestParam String toyId,
            @RequestParam int addQuantity) {

        inventoryService.restockToy(toyId, addQuantity);
        return "redirect:/inventory"; // go back to inventory page after restocking
    }

    // ── GET /suppliers — show all suppliers ───────────────────────────────────
    @GetMapping("/suppliers")
    public String listSuppliers(Model model) {
        model.addAttribute("suppliers",  supplierService.getAllSuppliers());
        model.addAttribute("totalCount", supplierService.getAllSuppliers().size());
        return "inventory/suppliers"; // loads templates/inventory/suppliers.html
    }

    // ── GET /suppliers/add — show the add supplier form ───────────────────────
    @GetMapping("/suppliers/add")
    public String showAddSupplierForm(Model model) {
        model.addAttribute("supplier", new Supplier()); // empty object for the form
        return "inventory/add-supplier";
    }

    // ── POST /suppliers/add — save the new supplier ───────────────────────────
    @PostMapping("/suppliers/add")
    public String addSupplier(@ModelAttribute Supplier supplier) {
        supplierService.addSupplier(supplier); // saves to suppliers.txt
        return "redirect:/suppliers";
    }

    // ── GET /suppliers/edit/{id} — show the edit form pre-filled ─────────────
    @GetMapping("/suppliers/edit/{supplierId}")
    public String showEditSupplierForm(
            @PathVariable String supplierId,
            Model model) {

        model.addAttribute("supplier", supplierService.getSupplierById(supplierId));
        return "inventory/edit-supplier";
    }

    // ── POST /suppliers/edit/{id} — save updated supplier data ───────────────
    @PostMapping("/suppliers/edit/{supplierId}")
    public String updateSupplier(
            @PathVariable String supplierId,
            @ModelAttribute Supplier supplier) {

        supplier.setSupplierId(supplierId); // make sure ID stays the same
        supplierService.updateSupplier(supplier);
        return "redirect:/suppliers";
    }

    // ── GET /suppliers/delete/{id} — delete a supplier ───────────────────────
    @GetMapping("/suppliers/delete/{supplierId}")
    public String deleteSupplier(@PathVariable String supplierId) {
        supplierService.deleteSupplier(supplierId);
        return "redirect:/suppliers";
    }
}