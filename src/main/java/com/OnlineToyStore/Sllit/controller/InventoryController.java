package com.OnlineToyStore.Sllit.controller;

import com.OnlineToyStore.Sllit.model.Supplier;
import com.OnlineToyStore.Sllit.service.EmailService;
import com.OnlineToyStore.Sllit.service.InventoryService;
import com.OnlineToyStore.Sllit.service.SupplierService;
import com.OnlineToyStore.Sllit.util.SessionHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private EmailService emailService;

    // ── Inventory overview — ADMIN ONLY ────────────────
    @GetMapping("/inventory")
    public String inventoryOverview(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String restocked,
            @RequestParam(required = false) String error,
            HttpSession session, Model model) {

        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }

        model.addAttribute("totalToys",       inventoryService.getTotalToyCount());
        model.addAttribute("lowStockCount",   inventoryService.getLowStockCount());
        model.addAttribute("outOfStockCount", inventoryService.getOutOfStockCount());
        model.addAttribute("totalUnits",      inventoryService.getTotalStockUnits());

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
        if (restocked != null) {
            model.addAttribute("successMessage", "Inventory updated successfully.");
        }
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        return "inventory/list";
    }

    // ── Restock — ADMIN ONLY ───────────────────────────
    @PostMapping("/inventory/restock")
    public String restockToy(
            @RequestParam String toyId,
            @RequestParam int addQuantity,
            HttpSession session) {

        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        if (addQuantity <= 0) {
            return "redirect:/inventory?error=Restock%20quantity%20must%20be%20at%20least%201";
        }
        inventoryService.restockToy(toyId, addQuantity);
        return "redirect:/inventory?restocked=true";
    }

    // ── Supplier list — ADMIN ONLY ─────────────────────
    @GetMapping("/suppliers")
    public String listSuppliers(HttpSession session, Model model) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        model.addAttribute("suppliers",  supplierService.getAllSuppliers());
        model.addAttribute("totalCount", supplierService.getAllSuppliers().size());
        return "inventory/suppliers";
    }

    // ── Add Supplier form — ADMIN ONLY ─────────────────
    @GetMapping("/suppliers/add")
    public String showAddSupplierForm(HttpSession session, Model model) {
        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        model.addAttribute("supplier", new Supplier());
        return "inventory/add-supplier";
    }

    // ── Save new supplier — ADMIN ONLY ─────────────────
    @PostMapping("/suppliers/add")
    public String addSupplier(
            @ModelAttribute Supplier supplier,
            HttpSession session) {

        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        supplierService.addSupplier(supplier);
        emailService.notifyAdminNewSupplier(
                supplier.getName(),
                supplier.getSupplyCategory(),
                supplier.getEmail());
        return "redirect:/suppliers";
    }

    // ── Edit Supplier form — ADMIN ONLY ────────────────
    @GetMapping("/suppliers/edit/{supplierId}")
    public String showEditSupplierForm(
            @PathVariable String supplierId,
            HttpSession session, Model model) {

        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        model.addAttribute("supplier",
                supplierService.getSupplierById(supplierId));
        return "inventory/edit-supplier";
    }

    // ── Save edited supplier — ADMIN ONLY ──────────────
    @PostMapping("/suppliers/edit/{supplierId}")
    public String updateSupplier(
            @PathVariable String supplierId,
            @ModelAttribute Supplier supplier,
            HttpSession session) {

        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        supplier.setSupplierId(supplierId);
        supplierService.updateSupplier(supplier);
        return "redirect:/suppliers";
    }

    // ── Delete supplier — ADMIN ONLY ───────────────────
    @GetMapping("/suppliers/delete/{supplierId}")
    public String deleteSupplier(
            @PathVariable String supplierId,
            HttpSession session) {

        if (!SessionHelper.isAdmin(session)) {
            return "redirect:/access-denied";
        }
        supplierService.deleteSupplier(supplierId);
        return "redirect:/suppliers";
    }
}