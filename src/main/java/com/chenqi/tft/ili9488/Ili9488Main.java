package com.chenqi.tft.ili9488;

import org.apache.log4j.Logger;

import java.io.IOException;

public class Ili9488Main {
    private static Logger LOG = Logger.getLogger(Ili9488Main.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        LOG.debug("Ili9488Main.main run");
        Ili9488Driver.getInstance().init();
        Ili9488Driver.getInstance().setWindow();

        Ili9488Driver.getInstance().drawImg18BitColor(DrawImg.getImgForBili());
        Thread.sleep(4000);

        //模拟可视化爬虫
        DrawImg.showSimulationVisualCrawler();
    }
}
