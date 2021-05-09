package com.chenqi.tft.ili9488;

import com.pi4j.io.gpio.*;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.wiringpi.Spi;
import org.apache.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;

public class Ili9488Driver {


    public static final int HEIGHT = 480;
    public static final int WIDTH = 320;

    private Ili9488Driver() {
    }

    private static Logger LOG = Logger.getLogger(Ili9488Driver.class);
    private static Ili9488Driver ili9488Driver = new Ili9488Driver();

    public static Ili9488Driver getInstance() {
        return ili9488Driver;
    }

    final static GpioPinDigitalOutput DC;
    final static GpioPinDigitalOutput RST;
    final static GpioPinDigitalOutput LED;
    // SPI device
    public static SpiDevice spi;

    static {
        // in order to use the Broadcom GPIO pin numbering scheme, we need to configure the
        // GPIO factory to use a custom configured Raspberry Pi GPIO provider
        RaspiGpioProvider raspiGpioProvider = new RaspiGpioProvider(RaspiPinNumberingScheme.BROADCOM_PIN_NUMBERING);
        GpioFactory.setDefaultProvider(raspiGpioProvider);

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        LED = gpio.provisionDigitalOutputPin(RaspiBcmPin.GPIO_18, "LED", PinState.HIGH);
        DC = gpio.provisionDigitalOutputPin(RaspiBcmPin.GPIO_24, "DC", PinState.HIGH);
        RST = gpio.provisionDigitalOutputPin(RaspiBcmPin.GPIO_25, "RST", PinState.HIGH);

        try {
            spi = SpiFactory.getInstance(SpiChannel.CS0, //片选，接的是CE0，
                    64000000,
                    SpiDevice.DEFAULT_SPI_MODE); // default tft9341Spi mode 0
        } catch (IOException e) {
            e.printStackTrace();
        }

        Spi.wiringPiSPISetup(0, 64000000);
    }

    /**
     * 写入指令
     *
     * @param date
     * @throws IOException
     */
    private void sendCommand(int date) throws IOException {
        DC.low();
        spi.write((byte) date);
    }

    /**
     * 写入数据
     *
     * @param date
     * @throws IOException
     */
    private void sendData(int date) throws IOException {
        DC.high();
        spi.write((byte) date);
    }

    private static void LcdReset() {
        try {
            RST.high();
            Thread.sleep(20);
            RST.low();
            Thread.sleep(20);
            RST.high();
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 18bit的颜色作图
     *
     * @param bufferedImage
     * @throws IOException
     */
    public void drawImg18BitColor(BufferedImage bufferedImage) throws IOException {
        System.out.println("start to write Lcd Img");

        long starttime = new Date().getTime();
        byte[] colorBytes = new byte[WIDTH * 3];
        int i = 0;

        DC.high();
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = WIDTH - 1; x >= 0; x--) {
                int rgb = bufferedImage.getRGB(y, x);
                byte red = (byte) ((rgb >> 16) & 0xff); //获取红色的色值
                byte green = (byte) ((rgb >> 8) & 0xff);
                byte blue = (byte) (rgb & 0xff);

                colorBytes[i] = red;
                i++;
                colorBytes[i] = green;
                i++;
                colorBytes[i] = blue;
                i++;
            }
            i = 0;
            spi.write(colorBytes);
        }
        LOG.debug("cost time : " + (new Date().getTime() - starttime));
    }

    public void setWindow() throws IOException {
        sendCommand(0x2A);       // Column addr set
        int x0 = 0;
        sendData(x0 >> 8);
        sendData(x0);              // XSTART
        int x1 = WIDTH - 1;
        sendData(x1 >> 8);
        sendData(x1);       // XEND
        sendCommand(0x2B);      // Row addr set
        sendData(0 >> 8);
        sendData(0);           // YSTART
        int y1 = HEIGHT - 1;
        sendData(y1 >> 8);
        sendData(y1);       // YEND
        sendCommand(0x2C);
    }

    private boolean isInit = false;

    public void init() throws IOException {
        if (isInit) {
            return;
        }
        LED.high();
        LcdReset();
        sendCommand(0xE0);
        sendData(0x00);
        sendData(0x03);
        sendData(0x09);
        sendData(0x08);
        sendData(0x16);
        sendData(0x0A);
        sendData(0x3F);
        sendData(0x78);
        sendData(0x4C);
        sendData(0x09);
        sendData(0x0A);
        sendData(0x08);
        sendData(0x16);
        sendData(0x1A);
        sendData(0x0F);

        sendCommand(0xE0);
        sendData(0x00);
        sendData(0x16);
        sendData(0x19);
        sendData(0x03);
        sendData(0x0F);
        sendData(0x05);
        sendData(0x32);
        sendData(0x45);
        sendData(0x46);
        sendData(0x04);
        sendData(0x0E);
        sendData(0x0D);
        sendData(0x35);
        sendData(0x37);
        sendData(0x0F);

        sendCommand(0xC0);//Power Control 1
        sendData(0x17); //Vreg1out
        sendData(0x15);//Verg2out

        sendCommand(0xC1);//Power Control 2
        sendData(0x41);//VGH,VGL

        sendCommand(0xC5); //Power Control 3
        sendData(0x00);
        sendData(0x12); //Vcom
        sendData(0x80);

        sendCommand(0x36);    //  Memory Access Control
        sendData(0x48);

        sendCommand(0x3A);    // *** INTERFACE PIXEL FORMAT: 0x66 -> 18 bit; 0x55 -> 16 bit
        sendData(0x66);

        sendCommand(0xB0); // Interface Mode Control
        sendData(0x00); // 0x80: SDO NOT USE; 0x00 USE SDO

        sendCommand(0xB1);  //Frame rate
        sendData(0xA0); //60Hz

        sendCommand(0xB4);  //Display Inversion Control
        sendData(0x02);   //2-dot

        sendCommand(0xB6);  //Display Function Control  RGB/MCU Interface Control
        sendData(0x02);  //MCU
        sendData(0x02);//Source,Gate scan direction
        sendData(0x3B);

        sendCommand(0xB7); //EntryMode
        sendData(0xC6);
//
//        sendCommand(0xC5);
//        sendData(0x00);
//        sendData(0x12);
//        sendData(0x80);

        sendCommand(0xE9);   // Set Image Function
        sendData(0x00);// Disable 24 bit data

        sendCommand(0x53);   // Write CTRL Display Value
        sendData(0x28); // BCTRL && DD on

        sendCommand(0x51);   // Write Display Brightness Value
        sendData(0xFF);

        sendCommand(0xF7);   // Write Display Brightness Value
        sendData(0xA9);
        sendData(0x51);
        sendData(0x2C);
        sendData(0x82);

        sendCommand(0x11);   // Exit Sleep
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendCommand(0x29);    // Display on

        isInit = true;
    }
}
