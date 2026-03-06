package com.kudche.cafebillingmanagement.Utils;

import android.content.Context;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.kudche.cafebillingmanagement.Models.Product;
import com.kudche.cafebillingmanagement.Models.Sale;
import com.kudche.cafebillingmanagement.Models.SaleItem;
import com.kudche.cafebillingmanagement.Database.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrinterUtils {

    public static DeviceConnection getSpecificPrinter() {
        BluetoothConnection[] bluetoothDevicesList = (new BluetoothPrintersConnections()).getList();
        if (bluetoothDevicesList != null) {
            for (BluetoothConnection device : bluetoothDevicesList) {
                if (device.getDevice().getAddress().equals(Constants.PRINTER_MAC_ADDRESS)) {
                    return device;
                }
            }
        }
        return null;
    }

    public static void printReceipt(Context context, Sale sale, List<SaleItem> items) {
        new Thread(() -> {
            try {
                DeviceConnection connection = getSpecificPrinter();
                
                if (connection == null) {
                    connection = BluetoothPrintersConnections.selectFirstPaired();
                }

                if (connection == null) return;

                EscPosPrinter printer = new EscPosPrinter(connection, 203, 48f, 32);
                
                StringBuilder itemsHtml = new StringBuilder();
                AppDatabase db = AppDatabase.getInstance(context);
                
                for (SaleItem item : items) {
                    Product product = db.productDao().getByIdSync(item.productId);
                    String name = product != null ? product.name : "Unknown Item";
                    if (name.length() > 16) name = name.substring(0, 13) + "..";
                    
                    itemsHtml.append("[L]").append(name)
                            .append("[C]").append(item.quantity)
                            .append("[R]").append(String.format(Locale.getDefault(), "%.2f", item.priceAtSale * item.quantity))
                            .append("\n");
                }

                String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date(sale.createdAt));

                String receipt = 
                        "[C]<b><font size='big'>" + Constants.CAFE_NAME + "</font></b>\n" +
                        "[C]" + Constants.CAFE_ADDRESS + "\n" +
                        "[C]Contact: " + Constants.CAFE_CONTACT + "\n" +
                        "[C]--------------------------------\n" +
                        "[L]Bill No: " + sale.invoiceNumber + "\n" +
                        "[L]Date: " + date + "\n" +
                        "[C]--------------------------------\n" +
                        "[L]<b>Item</b>[C]<b>Qty</b>[R]<b>Amount</b>\n" +
                        "[C]--------------------------------\n" +
                        itemsHtml.toString() +
                        "[C]--------------------------------\n" +
                        "[L]<b>TOTAL AMOUNT</b>[R]<b>" + String.format(Locale.getDefault(), "Rs. %.2f", sale.totalAmount) + "</b>\n" +
                        "[C]--------------------------------\n" +
                        "[C]Thank you! Visit Again\n" +
                        "[C]\n\n\n";

                printer.printFormattedTextAndCut(receipt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}