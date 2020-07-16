package fr.acourtial.cartagen.plugins;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;



import fr.acourtial.cartagen.plugins.RoadsToImages;
import fr.ign.cogit.cartagen.appli.core.geoxygene.CartAGenPlugin;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.appli.GeOxygeneApplication;
import fr.ign.cogit.geoxygene.style.Layer;

public class PluginAzelle extends JMenu{
    private static final long serialVersionUID = 1L;
	private GeOxygeneApplication application;
	
	
	public PluginAzelle(String title) {

		super(title);
        application = CartAGenPlugin.getInstance().getApplication();
        JMenu myMenu1 = new JMenu("roads object rasterization");
        this.add(myMenu1);
        JMenu myMenu1a = new JMenu("for GAN");
        myMenu1a.add(new JMenuItem(new ActionR2Iobj()));
        myMenu1.add(myMenu1a);
        JMenu myMenu1b = new JMenu("for UNET");
        myMenu1b.add(new JMenuItem(new ActionR2Iobjb()));
        myMenu1.add(myMenu1b);

        JMenu myMenu2 = new JMenu("roads windows rasterization");
        this.add(myMenu2);
        JMenu myMenu2b = new JMenu("for UNET");
        myMenu2b.add(new JMenuItem(new ActionR2Iwinb()));
        myMenu2.add(myMenu2b);
        
        JMenu myMenu3 = new JMenu("asymetrique");
        this.add(myMenu2);
        JMenu myMenu3a = new JMenu("a");
        myMenu3a.add(new JMenuItem(new Actionbazar()));
        myMenu2.add(myMenu3a);
        JMenu myMenu3b = new JMenu("b");
        myMenu3b.add(new JMenuItem(new Actionbazar()));
        myMenu3.add(myMenu3b);
        
	}

	public class Actionbazar extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			//
			System.out.println("J'ai cliqué sur mon menu 3");
			Layer layer = application.getMainFrame().getSelectedProjectFrame().getLayer("routes_250k_pyrenees");
            IFeatureCollection<? extends IFeature> roads = layer.getFeatureCollection();
            RoadsToImages ri=new RoadsToImages(roads);
            Object[] obj =ri.createBImages(256,10,3,60);
            List<BufferedImage> imagesGeneralised= (List<BufferedImage>)obj [0];

            int i = 0;
            for (BufferedImage bi : imagesGeneralised) {
                File outputfile2 = new File("D://deep_datasets//roads//generalised//road_" + i + ".png");
                i++;
                try {
                	ImageIO.write(bi, "png", outputfile2);
                } catch (IOException f) {
                	f.printStackTrace();
                }
            }
            i = 0;
            System.out.println("ecrit");
		}
	}
	
	
	public class ActionR2Iobj extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			
			Thread th = new Thread(new Runnable() {

                public void run() {
			
                	Layer layer_alpes_250k = application.getMainFrame().getSelectedProjectFrame().getLayer("routes_250k_alpes");
                	Layer layer_Bduni = application.getMainFrame().getSelectedProjectFrame().getLayer("routes_bduni_alpes");
                	IFeatureCollection<? extends IFeature> initialRoads = layer_Bduni.getFeatureCollection();
                	IFeatureCollection<? extends IFeature> generalisedRoads = layer_alpes_250k.getFeatureCollection();
                	String imagePath = null;

                	RoadsToImages ri=new RoadsToImages(initialRoads, generalisedRoads,imagePath);
                	List<BufferedImage> images = ri.createStickedBufferedImages(256, 5);
                	
                	int i = 0;
                	for (BufferedImage bi : images) {
                		File outputfile2 = new File("D://deep_datasets//roads//road_" + i + ".png");
                		i++;
                		try {
                			ImageIO.write(bi, "png", outputfile2);
                		} catch (IOException e) {
                			e.printStackTrace();
                		}
                	}
                }
			});
			th.start();
		}
	}
	
	
	
	public class ActionR2Iobjb extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			
			Thread th = new Thread(new Runnable() {

                public void run() {
                	Layer layer_alpes_250k = application.getMainFrame()
                    .getSelectedProjectFrame()
                    .getLayer("routes_250k_alpes");
                	Layer layer_Bduni = application.getMainFrame()
                    .getSelectedProjectFrame()
                    .getLayer("routes_bduni_alpes");
                	IFeatureCollection<? extends IFeature> initialRoads = layer_Bduni.getFeatureCollection();
                	IFeatureCollection<? extends IFeature> generalisedRoads = layer_alpes_250k.getFeatureCollection();
                	String imagePath = null;
                	RoadsToImages ri=new RoadsToImages(initialRoads, generalisedRoads,imagePath);
                	List<BufferedImage> imagesGeneralised = ri
                    .createGeneralisedBufferedImages(256, 5);
                	List<BufferedImage> imagesInitiales = ri
                    .createInitialBufferedImages(256, 5);
            
                	int i = 0;
                	for (BufferedImage bi : imagesGeneralised) {

                		File outputfile2 = new File("D://deep_datasets//roads//generalised//road_" + i + ".png");
                		i++;
                		try {
                			ImageIO.write(bi, "png", outputfile2);
                		} catch (IOException e) {
                			e.printStackTrace();
                		}
                	}
                	
                	i = 0;
                	for (BufferedImage bi : imagesInitiales) {

                		File outputfile2 = new File("D://deep_datasets//roads//initial//road_" + i + ".png");
                		i++;
                		try {
                			ImageIO.write(bi, "png", outputfile2);
                		} catch (IOException e) {
                			e.printStackTrace();
                		}
                	}

                }
			});
			th.start();
		}
	}

	public class ActionR2Iwinb extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("J'ai cliqué sur mon menu 2b");
			Layer layer_alpes_250k = application.getMainFrame().getSelectedProjectFrame().getLayer("routes_250k_alpes_nv");
            Layer layer_Bduni = application.getMainFrame().getSelectedProjectFrame().getLayer("routes_bduni_alpes_matched");
            IFeatureCollection<? extends IFeature> initialRoads = layer_Bduni.getFeatureCollection();
            IFeatureCollection<? extends IFeature> generalisedRoads = layer_alpes_250k.getFeatureCollection();
            String imagePath = null;
            RoadsToImages ri=new RoadsToImages(initialRoads, generalisedRoads,imagePath);
            Object[] obj =ri.createSquareImages(512,6,3,70);
            System.out.println("ecrit");
		}
	}
		
	public class ActionR2Iwin extends AbstractAction {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				//
				System.out.println("J'ai cliqu� sur mon menu 2a");
				Layer layer_alpes_250k = application.getMainFrame()
	                    .getSelectedProjectFrame()
	                    .getLayer("routes_250k_alpes_nv");
	                	Layer layer_Bduni = application.getMainFrame()
	                    .getSelectedProjectFrame()
	                    .getLayer("routes_bduni_alpes_matched");
	                	IFeatureCollection<? extends IFeature> initialRoads = layer_Bduni.getFeatureCollection();
	                	IFeatureCollection<? extends IFeature> generalisedRoads = layer_alpes_250k.getFeatureCollection();
	                	String imagePath = null;
	                	RoadsToImages ri=new RoadsToImages(initialRoads, generalisedRoads,imagePath);
	                	
	                	List<BufferedImage> imagesGeneralised=  ri.createdoubleImages(512,2,70);

	                	int i = 0;
	                	for (BufferedImage bi : imagesGeneralised ) {

	                		File outputfile2 = new File("D://deep_datasets//roads//road_" + i + ".png");
	                		i++;
	                		try {
	                			ImageIO.write(bi, "png", outputfile2);
	                		} catch (IOException f) {
	                			f.printStackTrace();
	                		}
	                	}

			}
	}
}