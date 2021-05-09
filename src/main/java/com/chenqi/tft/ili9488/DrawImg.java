package com.chenqi.tft.ili9488;

import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class DrawImg {
    private static Logger LOG = Logger.getLogger(Ili9488Main.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        BufferedImage bufferedImage = getImgForBili();
        ImageIO.write(bufferedImage, "jpg", new File("D:\\Poetry.jpg"));
    }

    /**
     * 模拟可视化爬虫
     * 获取jar包同路径下的所有后缀为jpg的图片并展示
     */
    public static void showSimulationVisualCrawler() throws IOException, InterruptedException {
        int width = Ili9488Driver.HEIGHT;
        int height = Ili9488Driver.WIDTH;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = bufferedImage.getGraphics();

        String path = Ili9488Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.substring(0, path.lastIndexOf(File.separator) + 1);
        System.out.println(path);
        File file = new File(path);
        File[] files = file.listFiles();

        int everyLinePicNum = 6;
        int eachPicWidth = width / everyLinePicNum;
        int eachPicHeight = height / everyLinePicNum;

        int contentNum = 483;
        int totalPicNum = 1742;

        int picNum = 0;
        for (File eachFile : files) {
            if (eachFile.getName().toLowerCase().endsWith("jpg")) {
                String imgPath = eachFile.getCanonicalPath();
                System.out.println("imgPath = " + imgPath);

                System.out.println("imgPath = " + imgPath);
                //读取原始位图
                Image srcImage = ImageIO.read(new File(imgPath));

                int picStarX = picNum * eachPicWidth % width;
                int picStartY = picNum / everyLinePicNum * eachPicHeight;

                //将原始位图缩小后绘制到bufferedImage对象中
                graphics.drawImage(srcImage, picStarX, picStartY, eachPicWidth, eachPicHeight, null);
                picNum++;

                graphics.setFont(new Font("微软雅黑", Font.PLAIN, 24));
                graphics.setColor(new Color(0x25FF22));
                graphics.clearRect(0, 293, 480, 25);
                graphics.drawString("已爬内容：" + (contentNum += 1) + " 已爬图片：" + (totalPicNum += (new Random()).nextInt(23)), 0, 316);

                Ili9488Driver.getInstance().drawImg18BitColor(bufferedImage);
            }
        }
    }

    /**
     * B站本视频封面图片
     * @return
     */
    public static BufferedImage getImgForBili() {
        LOG.debug("start to getImgForBili");
        int width = Ili9488Driver.HEIGHT;
        int height = Ili9488Driver.WIDTH;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setFont(new Font("微软雅黑", Font.BOLD, 60));
        g.setColor(new Color(0x25FF22));
        g.drawString("JAVA 玩转", 4, 60);

        //g.setFont(new Font("华文新魏", Font.PLAIN, 20));
        //g.setColor(new Color(0x64FFE7));
        g.drawString("树莓派小屏", 80, 120);

        g.setFont(new Font("微软雅黑", Font.BOLD, 60));
        g.setColor(new Color(0x67FFC6));
        g.drawString("爬虫可视化", 160, 190);

        g.setFont(new Font("微软雅黑", Font.PLAIN, 35));
        g.setColor(new Color(0xFFAC25));
        g.drawString("驱动芯片ILI9488", 0, 280);
        g.drawString("分辨率480x1320", 0, 240);
        g.drawString("陈琦玩派派", 290, 315);

        return image;
    }
}
