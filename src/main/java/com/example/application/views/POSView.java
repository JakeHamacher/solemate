package com.example.application.views;

import com.example.application.helpers.*;
import com.example.application.helpers.repos.*;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.frontend.installer.FileDownloader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Route("")
public class POSView extends VerticalLayout {
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SalespersonRepository salespersonRepository;

    // Components for the POS view
    private final TextField customerField = new TextField("Customer Name");
    private final TextField quantityField = new TextField("Quantity");
    private final ComboBox<Salesperson> salespersonComboBox = new ComboBox<>("Select Salesperson");
    private final Button completeButton = new Button("Complete Transaction");

    private Grid<Product> grid;
    private final Grid<TicketItem> ticketItemGrid = new Grid<>(TicketItem.class);

    // POS data tracking
    private final Map<Product, Integer> selectedItems = new HashMap<>();
    private Product selectedProduct;
    private Customer selectedCustomer;
    private Salesperson selectedSalesperson;

    // Tabs for POS and Inventory Management views
    private Tabs tabs;
    private Tab posTab;
    private Tab inventoryTab;

    private VerticalLayout posLayout;
    private VerticalLayout inventoryLayout;

    public POSView(ProductRepository productRepository, CustomerRepository customerRepository, 
                   TicketRepository ticketRepository, SalespersonRepository salespersonRepository) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.salespersonRepository = salespersonRepository;

        // Set up tabs
        posTab = new Tab("POS");
        inventoryTab = new Tab("Inventory Management");

        tabs = new Tabs(posTab, inventoryTab);
        tabs.setSelectedTab(posTab); // Default view is POS

        tabs.addSelectedChangeListener(event -> {
            Tab selectedTab = event.getSelectedTab();
            if (selectedTab.equals(posTab)) {
                showPOSView();
            } else if (selectedTab.equals(inventoryTab)) {
                showInventoryView();
            }
        });

        posLayout = new VerticalLayout();
        setupPOSView();

        inventoryLayout = new VerticalLayout();
        setupInventoryView();

        add(tabs, posLayout); // Initially display POS view
    }

    private void showPOSView() {
        removeAll();
        add(tabs, posLayout);
    }

    private void showInventoryView() {
        removeAll();
        add(tabs, inventoryLayout);
    }

    private void setupPOSView() {
        // Set up product grid for inventory selection
        grid = new Grid<>(Product.class);
        grid.setColumns("name", "price", "quantityInStock");
        grid.setItems(productRepository.findAll());
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        grid.addSelectionListener(event -> {
            event.getFirstSelectedItem().ifPresent(product -> {
                selectedProduct = product;
                Notification.show("Selected Product: " + product.getName(), 3000, Notification.Position.MIDDLE);
            });
        });

        // Salesperson ComboBox
        salespersonComboBox.setItems(salespersonRepository.findAll());
        salespersonComboBox.setItemLabelGenerator(Salesperson::getName);
        salespersonComboBox.addValueChangeListener(e -> selectedSalesperson = e.getValue());

        // Add to Ticket Button
        Button addToTicketButton = new Button("Add to Ticket", e -> addToTicket(quantityField));

        // Ticket Items Grid
        ticketItemGrid.setColumns("product.name", "quantity", "price");

        posLayout.add(customerField, grid, quantityField, salespersonComboBox, addToTicketButton, ticketItemGrid, completeButton);

        // Complete transaction button
        completeButton.addClickListener(e -> completeTransaction());
    }

    private void setupInventoryView() {
        // Inventory Management Grid
        Grid<Product> inventoryGrid = new Grid<>(Product.class);
        inventoryGrid.setColumns("name", "price", "quantityInStock");
        inventoryGrid.setItems(productRepository.findAll());
    
        // Input fields for adding or updating inventory
        TextField productNameField = new TextField("Product Name");
        TextField productPriceField = new TextField("Product Price");
        TextField productQuantityField = new TextField("Quantity In Stock");
    
        // Buttons for managing inventory
        Button addProductButton = new Button("Add Product", e -> addProduct(
            productNameField.getValue(),
            productPriceField.getValue(),
            productQuantityField.getValue()
        ));
    
        Button updateProductButton = new Button("Update Selected Product", e -> updateProduct(
            inventoryGrid.asSingleSelect().getValue(),
            productNameField.getValue(),
            productPriceField.getValue(),
            productQuantityField.getValue()
        ));
    
        Button deleteProductButton = new Button("Delete Selected Product", e -> deleteProduct(
            inventoryGrid.asSingleSelect().getValue()
        ));
    
        // Add selection listener to populate fields when a product is selected
        inventoryGrid.asSingleSelect().addValueChangeListener(event -> {
            Product selectedProduct = event.getValue();
            if (selectedProduct != null) {
                productNameField.setValue(selectedProduct.getName());
                productPriceField.setValue(String.valueOf(selectedProduct.getPrice()));
                productQuantityField.setValue(String.valueOf(selectedProduct.getQuantityInStock()));
            } else {
                productNameField.clear();
                productPriceField.clear();
                productQuantityField.clear();
            }
        });
    
        // Layout for inventory management view
        inventoryLayout.add(
            inventoryGrid,
            productNameField,
            productPriceField,
            productQuantityField,
            addProductButton,
            updateProductButton,
            deleteProductButton
        );
    }
    
    // Helper methods for inventory actions
    private void addProduct(String name, String price, String quantity) {
        try {
            if (name.isBlank() || price.isBlank() || quantity.isBlank()) {
                Notification.show("All fields are required", 3000, Notification.Position.MIDDLE);
                return;
            }
    
            Product product = new Product();
            product.setName(name);
            product.setPrice(Double.parseDouble(price));
            product.setQuantityInStock(Integer.parseInt(quantity));
    
            productRepository.save(product);
            Notification.show("Product added successfully", 3000, Notification.Position.MIDDLE);
    
            refreshInventoryGrid();
        } catch (NumberFormatException e) {
            Notification.show("Invalid price or quantity", 3000, Notification.Position.MIDDLE);
        }
    }
    
    private void updateProduct(Product product, String name, String price, String quantity) {
        if (product == null) {
            Notification.show("No product selected", 3000, Notification.Position.MIDDLE);
            return;
        }
    
        try {
            product.setName(name);
            product.setPrice(Double.parseDouble(price));
            product.setQuantityInStock(Integer.parseInt(quantity));
    
            productRepository.save(product);
            Notification.show("Product updated successfully", 3000, Notification.Position.MIDDLE);
    
            refreshInventoryGrid();
        } catch (NumberFormatException e) {
            Notification.show("Invalid price or quantity", 3000, Notification.Position.MIDDLE);
        }
    }
    
    private void deleteProduct(Product product) {
        if (product == null) {
            Notification.show("No product selected", 3000, Notification.Position.MIDDLE);
            return;
        }
    
        productRepository.delete(product);
        Notification.show("Product deleted successfully", 3000, Notification.Position.MIDDLE);
    
        refreshInventoryGrid();
    }
    
    private void refreshInventoryGrid() {
        Grid<Product> inventoryGrid = new Grid<>(Product.class);
        inventoryGrid.setColumns("name", "price", "quantityInStock");
        inventoryGrid.setItems(productRepository.findAll());
    }
    
    private void addToTicket(TextField quantityField) {
        try {
        if (selectedProduct == null) {
            Notification.show("Please select a product", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Validate the quantity
        int quantity = Integer.parseInt(quantityField.getValue());
        if (quantity <= 0 || quantity > selectedProduct.getQuantityInStock()) {
            Notification.show("Invalid quantity", 3000, Notification.Position.MIDDLE);
            return;
        }

        // Validate the customer
        String customerName = customerField.getValue().trim();
        if (customerName.isEmpty()) {
            Notification.show("Please enter a customer name", 3000, Notification.Position.MIDDLE);
            return;
        }

        selectedCustomer = customerRepository.findByName(customerName);
        if (selectedCustomer == null) {
            // Create a new customer if not found
            selectedCustomer = new Customer();
            selectedCustomer.setId(generateCustomerId()); // Assign a new unique ID
            selectedCustomer.setName(customerName);
            customerRepository.save(selectedCustomer);
            Notification.show("New customer added: " + customerName, 3000, Notification.Position.MIDDLE);
        } else {
            Notification.show("Customer selected: " + customerName, 3000, Notification.Position.MIDDLE);
        }

        // Add the product and quantity to the selected items map
        selectedItems.put(selectedProduct, quantity);

        // Update the ticket items grid
        List<TicketItem> ticketItems = selectedItems.entrySet().stream()
            .map(entry -> new TicketItem(entry.getKey(), entry.getValue(), null))
            .collect(Collectors.toList());
        ticketItemGrid.setItems(ticketItems);

        Notification.show("Product added to ticket", 3000, Notification.Position.MIDDLE);

        // Reset selections
        grid.deselectAll();
        selectedProduct = null;
        quantityField.clear();
    } catch (NumberFormatException e) {
        Notification.show("Please enter a valid quantity", 3000, Notification.Position.MIDDLE);
    } catch (Exception e) {
        Notification.show("An error occurred: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
        e.printStackTrace();
    }
}

    
    private Long generateCustomerId() {
        return UUID.randomUUID().getMostSignificantBits();
    }
      

    private void completeTransaction() {
        try {
            // Validate selected customer
            if (selectedCustomer == null) {
                Notification.show("Please add or select a customer", 3000, Notification.Position.MIDDLE);
                return;
            }
    
            // Validate selected salesperson
            if (selectedSalesperson == null) {
                Notification.show("Please select a salesperson", 3000, Notification.Position.MIDDLE);
                return;
            }
    
            // Iterate over selected items and update their stock
            for (Map.Entry<Product, Integer> entry : selectedItems.entrySet()) {
                Product product = entry.getKey();
                int quantitySold = entry.getValue();
                int updatedQuantity = product.getQuantityInStock() - quantitySold;
    
                // Check if there is enough stock to complete the transaction
                if (updatedQuantity < 0) {
                    Notification.show("Not enough stock for product: " + product.getName(), 3000, Notification.Position.MIDDLE);
                    return;
                }
    
                // Update the product quantity in the database
                product.setQuantityInStock(updatedQuantity);
                productRepository.save(product); // Save the updated product in the repository
            }
    
            Notification.show("Transaction completed successfully!", 3000, Notification.Position.MIDDLE);
    
            // Clear the POS view to prepare for a new transaction
            // Trigger receipt download
            downloadReceipt();
            clearPOSView();
    
        } catch (Exception e) {
            // Catch and log any errors for debugging
            e.printStackTrace();
            Notification.show("Error completing transaction: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private String generateReceiptContent() {
        StringBuilder receipt = new StringBuilder();
    
        // Start HTML document
        receipt.append("<html><head><style>")
               .append("body { font-family: Arial, sans-serif; margin: 20px; }")
               .append("h1 { text-align: center; }")
               .append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }")
               .append("th, td { padding: 8px; border: 1px solid #ddd; text-align: left; }")
               .append("th { background-color: #f2f2f2; }")
               .append("</style></head><body>");
    
        // Add title
        receipt.append("<h1>------ POS RECEIPT ------</h1>");
        receipt.append("<p><strong>Customer:</strong> ").append(selectedCustomer.getName()).append("</p>");
        receipt.append("<p><strong>Salesperson:</strong> ").append(selectedSalesperson.getName()).append("</p>");
        
        // Items Purchased table
        receipt.append("<h3>Items Purchased:</h3>");
        receipt.append("<table><tr><th>Item</th><th>Quantity</th><th>Price</th><th>Total</th></tr>");
        
        double totalPrice = 0.0;
        for (Map.Entry<Product, Integer> entry : selectedItems.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            double itemPrice = product.getPrice();
            double itemTotalPrice = itemPrice * quantity;
            totalPrice += itemTotalPrice;
    
            receipt.append(String.format("<tr><td>%s</td><td>%d</td><td>%.2f</td><td>%.2f</td></tr>", 
                                          product.getName(), quantity, itemPrice, itemTotalPrice));
        }
        
        // Total Price section
        receipt.append("</table>");
        receipt.append("<p><strong>Total Price:</strong> ").append(String.format("%.2f", totalPrice)).append("</p>");
        
        // Thank you note
        receipt.append("<hr><p style=\"text-align:center;\">------ Thank you for your purchase! ------</p>");
        
        // End HTML document
        receipt.append("</body></html>");
        
        return receipt.toString();
    }
    
    
    
    private void downloadReceipt() {
        // Generate the receipt content
        String receiptContent = generateReceiptContent();
        
        // Create a Blob from the receipt content
        String js = String.format(
            "var blob = new Blob([new TextEncoder().encode('%s')], { type: 'text/plain' });"
            + "var link = document.createElement('a');"
            + "link.href = URL.createObjectURL(blob);"
            + "link.download = 'receipt.html';"
            + "link.click();",
            receiptContent.replace("\n", "%0A").replace("\r", "%0D") // Properly encode line breaks
        );
        
        // Execute JavaScript in the browser to trigger the download
        UI.getCurrent().getPage().executeJs(js);
    }
    
    
    
    
    private void clearPOSView() {
        customerField.clear();
        quantityField.clear();
        selectedProduct = null;
        selectedCustomer = null;
        selectedSalesperson = null;
        grid.deselectAll();
        ticketItemGrid.setItems(List.of());
        selectedItems.clear();
    }
    
}
