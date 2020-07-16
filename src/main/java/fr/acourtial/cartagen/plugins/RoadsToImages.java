package fr.acourtial.cartagen.plugins;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import org.geotools.feature.FeatureCollection;

import fr.acourtial.cartagen.plugins.CoordinateTransformation;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IEnvelope;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.geomaggr.IMultiCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IOrientableCurve;
import fr.ign.cogit.geoxygene.api.spatial.geomprim.IPoint;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
/**
 * this class provides images of a road that can be used for deep learning
 * training or prediction.
 * 
 * @author ACourtial
 *
 */
public class RoadsToImages {
    private IFeatureCollection<? extends IFeature> initialRoads,generalisedRoads;
    private String imagePath;
    private Color backgroundColor = Color.WHITE;
    private Color casingColor = Color.BLACK;
    private Color innerColor = Color.RED;
    private Color innerColor2 = Color.GREEN;
    private int casingWidth = 3, innerWidth = 2;
    
    
    public RoadsToImages(IFeatureCollection<? extends IFeature> initialRoads,IFeatureCollection<? extends IFeature> generalisedRoads, String imagePath) {
        super();
        this.initialRoads = initialRoads;
        this.generalisedRoads = generalisedRoads;
        this.setImagePath(imagePath);
    }
    public RoadsToImages(IFeatureCollection<? extends IFeature> roads) {
        super();
        this.generalisedRoads = roads;
        this.setImagePath(imagePath);
    }
    
	public List<BufferedImage> createStickedBufferedImages(int imageSize,int gap) {
        List<BufferedImage> listImages = new ArrayList<BufferedImage>();
        for (IFeature line : generalisedRoads) {
            ILineString polyline = (ILineString) ((IMultiCurve<IOrientableCurve>) line.getGeom()).get(0);

            if(polyline.length()>200 ) {//&& (int)(line.getAttribute("montagne"))==0
            CoordinateTransformation transform = CoordinateTransformation.getCoordTransfoFromPolyline(polyline, gap, imageSize);
            Collection<IFeature> roads = (Collection<IFeature>) initialRoads.select(polyline.envelope());
            
            double ratio=(int)(1/transform.getHomotheticRatio());
            int l=0;
            for (IFeature road : roads) {
            	ILineString polyline1 = (ILineString) ((IMultiCurve<IOrientableCurve>) road.getGeom()).get(0); 
            	l+=polyline1.length();}
            
            if (roads.isEmpty() ) {continue;}
            Collection<IFeature> other_roads = (Collection<IFeature>) generalisedRoads.select(polyline.envelope());

            // Generate a blank RGB image
            BufferedImage bi = new BufferedImage(2 * imageSize, imageSize,BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bi.createGraphics();
            g2d.setBackground(this.getBackgroundColor());
            g2d.clearRect(0, 0, 2 * imageSize, imageSize);

            // first generate the left part of the image
            IDirectPositionList viewDirectPositionList = transform.transform(line.getGeom().coord());
            // Shape shape = toPolylineShape(viewDirectPositionList);
            int numPoints = viewDirectPositionList.size();
            int[] xpoints = new int[numPoints];
            int[] ypoints = new int[numPoints];
            for (int i = 0; i < viewDirectPositionList.size(); i++) {
                IDirectPosition p = viewDirectPositionList.get(i);
                xpoints[i] = (int) p.getX();
                ypoints[i] = (int) p.getY();
            }
            int nv=Integer.parseInt(((String)(line.getAttribute("nv").toString())))*2;
            g2d.setColor(this.getInnerColor());
            //AC//
            g2d.setColor(this.getInnerColor());
            //ratio
            //g2d.setStroke(new BasicStroke((int)(ratio/2)));
            //niveau
            g2d.setStroke(new BasicStroke(nv));
            //both
            //g2d.setStroke(new BasicStroke((nv+(int)ratio/2)/2));
            g2d.drawPolyline(xpoints, ypoints, numPoints);

            //g2d.setStroke(new BasicStroke(this.getInnerWidth()));
            //g2d.drawPolyline(xpoints, ypoints, numPoints);

            //AC//
            for (IFeature road : other_roads) {
                ILineString geom = (ILineString) ((IMultiCurve<IOrientableCurve>) road.getGeom()).get(0);
                IDirectPositionList viewDirectPositionList1 = transform.transform(geom.coord());

                int numPoints1 = viewDirectPositionList1.size();
                int[] xpoints1 = new int[numPoints1];
                int[] ypoints1 = new int[numPoints1];
                for (int i = 0; i < viewDirectPositionList1.size(); i++) {
                    IDirectPosition p1 = viewDirectPositionList1.get(i);

                    xpoints1[i] = (int) p1.getX();
                    ypoints1[i] = (int) p1.getY();
                }
                int nv2=Integer.parseInt(((String)(road.getAttribute("nv").toString())))*2;
                g2d.setColor(this.getInnerColor());
                //ratio
                //g2d.setStroke(new BasicStroke((int)ratio/2));
                //niveau
                g2d.setStroke(new BasicStroke(nv2));
                //both
                //g2d.setStroke(new BasicStroke((nv2+(int)ratio/2)/2));
                //g2d.setStroke(new BasicStroke(this.getInnerWidth()));
                g2d.drawPolyline(xpoints1, ypoints1, numPoints1);
            }
            
            
            // then generate the right part of the image with the initial roads
            for (IFeature road : roads) {
                ILineString geom = (ILineString) ((IMultiCurve<IOrientableCurve>) road.getGeom()).get(0);
                IDirectPositionList viewDirectPositionList1 = transform.transform(geom.coord());
                //System.out.println(viewDirectPositionList);
                int numPoints1 = viewDirectPositionList1.size();
                int[] xpoints1 = new int[numPoints1];
                int[] ypoints1 = new int[numPoints1];
                for (int i = 0; i < viewDirectPositionList1.size(); i++) {
                    IDirectPosition p1 = viewDirectPositionList1.get(i);

                    xpoints1[i] = (int) p1.getX() + imageSize;
                    ypoints1[i] = (int) p1.getY();
                }
                int nv2=Integer.parseInt(((String)(road.getAttribute("nv").toString())))*2;
                g2d.setColor(this.getInnerColor());
                //ratio
                //g2d.setStroke(new BasicStroke((int)ratio/2));
                //niveau
                //g2d.setStroke(new BasicStroke(nv2));
                //g2d.setStroke(new BasicStroke(this.getInnerWidth()));
                //both
                g2d.setStroke(new BasicStroke((nv2+(int)ratio/2)/2));
                g2d.drawPolyline(xpoints1, ypoints1, numPoints1);
            }

            listImages.add(bi);
        }}
        return listImages;
    }
    

	public List<BufferedImage> createInitialBufferedImages(int imageSize,int gap) {
        List<BufferedImage> listImages = new ArrayList<BufferedImage>();

        for (IFeature line : generalisedRoads) {
        	System.out.println("img");
            ILineString polyline = (ILineString) ((IMultiCurve<IOrientableCurve>) line.getGeom()).get(0);

            CoordinateTransformation transform = CoordinateTransformation.getCoordTransfoFromPolyline(polyline, gap, imageSize);
            
            //AC//
            double ratio=(int)(1/transform.getHomotheticRatio());
            Collection<IFeature> roads = (Collection<IFeature>) initialRoads.select(polyline.envelope());
            if(polyline.length()<200 || roads.isEmpty() ) {//||(int)(line.getAttribute("montagne"))==0
            	//System.out.println ("noimg");
            	continue;
            }

            // Generate a blank RGB image
            BufferedImage bi = new BufferedImage(imageSize, imageSize,BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bi.createGraphics();
            g2d.setBackground(this.getBackgroundColor());
            g2d.clearRect(0, 0, imageSize, imageSize);
            Object[][]dpoly=new Object[3][roads.size()];
            for (IFeature road : roads) {
                ILineString geom = (ILineString) ((IMultiCurve<IOrientableCurve>) road.getGeom()).get(0);
                IDirectPositionList viewDirectPositionList1 = transform.transform(geom.coord());
                // System.out.println(viewDirectPositionList);
                int numPoints1 = viewDirectPositionList1.size();
                int[] xpoints1 = new int[numPoints1];
                int[] ypoints1 = new int[numPoints1];
                for (int i = 0; i < viewDirectPositionList1.size(); i++) {
                    IDirectPosition p1 = viewDirectPositionList1.get(i);
                    xpoints1[i] = (int) p1.getX();
                    ypoints1[i] = (int) p1.getY();
                }
                //int nv=Integer.parseInt(((String)(road.getAttribute("nv").toString())))*2;
                g2d.setColor(this.getInnerColor());
                g2d.setStroke(new BasicStroke((((int)ratio*2))));//nv+(int)ratio/2)/2
                g2d.drawPolyline(xpoints1, ypoints1, numPoints1);
            }
            for (IFeature road : roads) {
                ILineString geom = (ILineString) ((IMultiCurve<IOrientableCurve>) road.getGeom()).get(0);
                IDirectPositionList viewDirectPositionList1 = transform.transform(geom.coord());
                // System.out.println(viewDirectPositionList);
                int numPoints1 = viewDirectPositionList1.size();
                int[] xpoints1 = new int[numPoints1];
                int[] ypoints1 = new int[numPoints1];
                for (int i = 0; i < viewDirectPositionList1.size(); i++) {
                    IDirectPosition p1 = viewDirectPositionList1.get(i);
                    xpoints1[i] = (int) p1.getX();
                    ypoints1[i] = (int) p1.getY();
                }
                g2d.setColor(this.getInnerColor2());
                g2d.setStroke(new BasicStroke(1));//nv+(int)ratio/2)/2
                g2d.drawPolyline(xpoints1, ypoints1, numPoints1);
            }

            

            listImages.add(bi);
        }
        return listImages;
    }
	
	
    private Color getInnerColor2() {
		return this.innerColor2;
	}

	public List<BufferedImage> createGeneralisedBufferedImages(int imageSize,
            int gap) {
        List<BufferedImage> listImages = new ArrayList<BufferedImage>();

        for (IFeature line : generalisedRoads) {
            ILineString polyline = (ILineString) ((IMultiCurve<IOrientableCurve>) line.getGeom()).get(0);
            // get the transform for the geographic coordinates to the image
            // coordinates
            CoordinateTransformation transform = CoordinateTransformation
                    .getCoordTransfoFromPolyline(polyline, gap, imageSize);
            
            //AC//
            double ratio=(int)(1/transform.getHomotheticRatio());
            Collection<IFeature> roadsG = (Collection<IFeature>) generalisedRoads.select(polyline.envelope());
            Collection<IFeature> roads = (Collection<IFeature>) initialRoads.select(polyline.envelope());
            if(polyline.length()<200 || roads.isEmpty()) {continue;}//|| (int)(line.getAttribute("montagne"))==0
            // System.out.println(transform);
            // Generate a blank RGB image
            BufferedImage bi = new BufferedImage(imageSize, imageSize,BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bi.createGraphics();
            g2d.setBackground(this.getBackgroundColor());
            g2d.clearRect(0, 0, imageSize, imageSize);

            //AC//
            for (IFeature road : roadsG) {
                ILineString geom = (ILineString) ((IMultiCurve<IOrientableCurve>) road.getGeom()).get(0);
                IDirectPositionList viewDirectPositionList1 = transform.transform(geom.coord());
                // System.out.println(viewDirectPositionList);
                int numPoints1 = viewDirectPositionList1.size();
                int[] xpoints1 = new int[numPoints1];
                int[] ypoints1 = new int[numPoints1];
                for (int i = 0; i < viewDirectPositionList1.size(); i++) {
                    IDirectPosition p1 = viewDirectPositionList1.get(i);
                    xpoints1[i] = (int) p1.getX();
                    ypoints1[i] = (int) p1.getY();
                }
                //int nv=Integer.parseInt(((String)(road.getAttribute("nv").toString())))*2;
                g2d.setColor(this.getInnerColor());
                g2d.setStroke(new BasicStroke((((int)ratio/2))));//nv+(int)ratio/2)/2
                g2d.drawPolyline(xpoints1, ypoints1, numPoints1);
            }
            IDirectPositionList viewDirectPositionList = transform.transform(line.getGeom().coord());
            // System.out.println(viewDirectPositionList);
            // Shape shape = toPolylineShape(viewDirectPositionList);
            int numPoints = viewDirectPositionList.size();
            int[] xpoints = new int[numPoints];
            int[] ypoints = new int[numPoints];
            for (int i = 0; i < viewDirectPositionList.size(); i++) {
                IDirectPosition p = viewDirectPositionList.get(i);
                xpoints[i] = (int) p.getX();
                ypoints[i] = (int) p.getY();
            }
            //AC//
            //int nv=Integer.parseInt(((String)(line.getAttribute("nv").toString())))*2;
            g2d.setColor(this.getInnerColor());
            g2d.setStroke(new BasicStroke(((int)ratio/2)/2));
            g2d.drawPolyline(xpoints, ypoints, numPoints); 
            
            
            listImages.add(bi);
        }
        return listImages;
    }

	public Object[]  createBImages(int nbpix,int imageSize,int gap, int recouvrement) {
    	System.out.println("dedans");
    	List<BufferedImage> listImages1 = new ArrayList<BufferedImage>();
    	IEnvelope carte=generalisedRoads.envelope();
    	double xmin=carte.minX();
    	double ymin=carte.minY();
    	double xmax=carte.maxX();
    	double ymax=carte.maxY();
    	IEnvelope fenetre= (IEnvelope) carte.clone();
    	int nwimageSize=imageSize*nbpix;
    	double pas =nwimageSize- nwimageSize*recouvrement/100;
    	IDirectPosition p1=carte.getLowerCorner();
    	IDirectPosition p2=carte.getUpperCorner();
    	p2.setCoordinate(xmin+nwimageSize, ymin+(nwimageSize));
    	while (p1.getX()<xmax){
    		p1.setCoordinate(p1.getX(), ymin);
    		p2.setCoordinate(p2.getX(), ymin+(nwimageSize));
    		while(p1.getY()<ymax){
    		    fenetre.setLowerCorner(p1);
    		    fenetre.setUpperCorner(p2);
    		    BufferedImage bi1 = new BufferedImage(nbpix, nbpix,BufferedImage.TYPE_INT_RGB);
    		    Graphics2D g2d1 = bi1.createGraphics();
    		    g2d1.setBackground(this.getBackgroundColor());
                g2d1.clearRect(0, 0, nbpix, nbpix);
                Collection<IFeature> roadsG =(Collection<IFeature>)  generalisedRoads.select((IEnvelope) fenetre);
                int seuil=0;
                int count=0;
                if(roadsG.isEmpty()) {
                	p1.setCoordinate(p1.getX(), p1.getY()+pas);
                	p2.setCoordinate(p2.getX(), p2.getY()+pas);
                	continue;
                }
                CoordinateTransformation transform = CoordinateTransformation.getCoordTransfoFromPolygon((IPolygon)(fenetre.getGeom()), gap, nbpix);
    			for (IFeature road : roadsG) {
                    ILineString geom = (ILineString) ((IMultiCurve<IOrientableCurve>) road.getGeom()).get(0);
                    IDirectPositionList viewDirectPositionList1 = transform.transform(geom.coord());
                    int numPoints1 = viewDirectPositionList1.size();
                    int[] xpoints1 = new int[numPoints1];
                    int[] ypoints1 = new int[numPoints1];
                    for (int i = 0; i < viewDirectPositionList1.size(); i++) {
                        IDirectPosition p3 = viewDirectPositionList1.get(i);
                        xpoints1[i] = (int) p3.getX();
                        ypoints1[i] = (int) p3.getY();
                    }
                    int nv=Integer.parseInt(((String)(road.getAttribute("nv").toString())))*2;
                    g2d1.setColor(this.getInnerColor());
                    //g2d.setStroke(new BasicStroke((this.getInnerWidth())));
                    g2d1.setStroke(new BasicStroke(nv));
                    g2d1.drawPolyline(xpoints1, ypoints1, numPoints1);

                }

    			listImages1.add(bi1);
                //}//}
        		p1.setCoordinate(p1.getX(), p1.getY()+pas);
        		p2.setCoordinate(p2.getX(), p2.getY()+pas);
    		}
    		p1.setCoordinate(p1.getX()+pas, p1.getY());
    		p2.setCoordinate(p2.getX()+pas, p2.getY());
    	}
    	Object[] monResultat = { listImages1};
    	return monResultat;
    	} 
	
	
    public Object[]  createSquareImages(int nbpix,int imageSize,int gap, int recouvrement) {

    	List<BufferedImage> listImages1 = new ArrayList<BufferedImage>();
    	List<BufferedImage> listImages2 = new ArrayList<BufferedImage>();
    	IEnvelope carte=initialRoads.envelope();
    	double xmin=carte.minX();
    	double ymin=carte.minY();
    	double xmax=carte.maxX();
    	double ymax=carte.maxY();
    	IEnvelope fenetre= (IEnvelope) carte.clone();
    	int nwimageSize=imageSize*nbpix;
    	double pas =nwimageSize- nwimageSize*recouvrement/100;
    	IDirectPosition p1=carte.getLowerCorner();
    	IDirectPosition p2=carte.getUpperCorner();
    	p2.setCoordinate(xmin+nwimageSize, ymin+(nwimageSize));
    	int j =0;
    	while (p1.getX()<xmax){
    		p1.setCoordinate(p1.getX(), ymin);
    		p2.setCoordinate(p2.getX(), ymin+(nwimageSize));
    		while(p1.getY()<ymax){
    		    fenetre.setLowerCorner(p1);
    		    fenetre.setUpperCorner(p2);
    		    BufferedImage bi1 = new BufferedImage(nbpix, nbpix,BufferedImage.TYPE_INT_RGB);
    		    Graphics2D g2d1 = bi1.createGraphics();
    		    g2d1.setBackground(this.getBackgroundColor());
                g2d1.clearRect(0, 0, nbpix, nbpix);
    		    BufferedImage bi2 = new BufferedImage(nbpix, nbpix,BufferedImage.TYPE_INT_RGB);
    		    Graphics2D g2d2 = bi2.createGraphics();
    		    g2d2.setBackground(this.getBackgroundColor());
                g2d2.clearRect(0, 0, nbpix, nbpix);
                Collection<IFeature> roadsG =(Collection<IFeature>)  generalisedRoads.select((IEnvelope) fenetre);
                int seuil=0;
                int count=0;
                for (IFeature road : roadsG) {
                	seuil= (int) (seuil+ (long)(road.getAttribute("sinuosite")));
                	count+=1;
                	}
                int meansinuose=0;
                if(count>0) {
                	meansinuose=seuil/count;
                }
                //if (seuil/count>70) {
                Collection<IFeature> roadsI =(Collection<IFeature>)  initialRoads.select((IEnvelope) fenetre);
                if(roadsG.isEmpty() || roadsI.isEmpty()||meansinuose>80) {
                	p1.setCoordinate(p1.getX(), p1.getY()+pas);
                	p2.setCoordinate(p2.getX(), p2.getY()+pas);
                	continue;
                }
                CoordinateTransformation transform = CoordinateTransformation.getCoordTransfoFromPolygon((IPolygon)(fenetre.getGeom()), gap, nbpix);
    			for (IFeature road : roadsG) {
                    ILineString geom = (ILineString) ((IMultiCurve<IOrientableCurve>) road.getGeom()).get(0);
                    IDirectPositionList viewDirectPositionList1 = transform.transform(geom.coord());
                    int numPoints1 = viewDirectPositionList1.size();
                    int[] xpoints1 = new int[numPoints1];
                    int[] ypoints1 = new int[numPoints1];
                    for (int i = 0; i < viewDirectPositionList1.size(); i++) {
                        IDirectPosition p3 = viewDirectPositionList1.get(i);
                        xpoints1[i] = (int) p3.getX();
                        ypoints1[i] = (int) p3.getY();
                    }
                    int nv=Integer.parseInt(((String)(road.getAttribute("nv").toString())))*2;
                    g2d1.setColor(this.getInnerColor());
                    //g2d.setStroke(new BasicStroke((this.getInnerWidth())));
                    g2d1.setStroke(new BasicStroke(nv));
                    g2d1.drawPolyline(xpoints1, ypoints1, numPoints1);

                }
    			for (IFeature road : roadsI) {
                    ILineString geom = (ILineString) ((IMultiCurve<IOrientableCurve>) road.getGeom()).get(0);
                    IDirectPositionList viewDirectPositionList1 = transform.transform(geom.coord());
                    int numPoints1 = viewDirectPositionList1.size();
                    int[] xpoints1 = new int[numPoints1];
                    int[] ypoints1 = new int[numPoints1];
                    for (int i = 0; i < viewDirectPositionList1.size(); i++) {
                        IDirectPosition p3 = viewDirectPositionList1.get(i);
                        xpoints1[i] = (int) p3.getX();
                        ypoints1[i] = (int) p3.getY();
                    }
                    int nv=Integer.parseInt(((String)(road.getAttribute("nv").toString())))*2;
                    g2d2.setColor(this.getInnerColor());
                    //g2d.setStroke(new BasicStroke((this.getInnerWidth())));
                    g2d2.setStroke(new BasicStroke(nv));
                    g2d2.drawPolyline(xpoints1, ypoints1, numPoints1);

                }
    			File outputfile1 = new File("D://deep_datasets//roads//generalised//road_" + j + ".png");

    			try {
                	ImageIO.write(bi1, "png", outputfile1);
                } catch (IOException f) {
                	f.printStackTrace();
                }
            	File outputfile2 = new File("D://deep_datasets//roads//initial//road_" + j + ".png");
                j++;
                try {
                	ImageIO.write(bi2, "png", outputfile2);
                } catch (IOException f) {
                	f.printStackTrace();
                }
//    			listImages1.add(bi1);listImages2.add(bi2);
                //}//}
        		p1.setCoordinate(p1.getX(), p1.getY()+pas);
        		p2.setCoordinate(p2.getX(), p2.getY()+pas);
    		}
    		p1.setCoordinate(p1.getX()+pas, p1.getY());
    		p2.setCoordinate(p2.getX()+pas, p2.getY());
    	}
    	Object[] monResultat = { listImages1 , listImages2 };
    	return monResultat ;
    	} 
    
    public List<BufferedImage>  createdoubleImages(int imageSize,int gap, int recouvrement) {

    	List<BufferedImage> listImages = new ArrayList<BufferedImage>();

    	IEnvelope carte=initialRoads.envelope();
    	double xmin=carte.minX();
    	double ymin=carte.minY();
    	double xmax=carte.maxX();
    	double ymax=carte.maxY();
    	IEnvelope fenetre= (IEnvelope) carte.clone();
    	int nwimageSize=imageSize*5;
    	double pas =nwimageSize- nwimageSize*recouvrement/100;
    	IDirectPosition p1=carte.getLowerCorner();
    	IDirectPosition p2=carte.getUpperCorner();
    	p2.setCoordinate(xmin+nwimageSize, ymin+(nwimageSize));
    	while (p1.getX()<xmax){
    		p1.setCoordinate(p1.getX(), ymin);
    		p2.setCoordinate(p2.getX(), ymin+(nwimageSize));
    		while(p1.getY()<ymax){
    		    fenetre.setLowerCorner(p1);
    		    fenetre.setUpperCorner(p2);
    		    BufferedImage bi = new BufferedImage(imageSize*2, imageSize,BufferedImage.TYPE_INT_RGB);
    		    Graphics2D g2d = bi.createGraphics();
    		    g2d.setBackground(this.getBackgroundColor());
                g2d.clearRect(0, 0, imageSize*2, imageSize);
    		    
                Collection<IFeature> roadsG =(Collection<IFeature>)  generalisedRoads.select((IEnvelope) fenetre);
                
                int seuil=0;
                int count=0;
                for (IFeature road : roadsG) {
                	//seuil= (int) (seuil+ (long)(road.getAttribute("sinuosite")));
                	count+=1;
                	}
                if(count>0) {
                if (true){//seuil/count>70) {
                	Collection<IFeature> roadsI =(Collection<IFeature>)  initialRoads.select((IEnvelope) fenetre);
                
                //if(roadsG.isEmpty() || roadsI.isEmpty()) {continue;}
            	CoordinateTransformation transform = CoordinateTransformation.getCoordTransfoFromPolygon((IPolygon)(fenetre.getGeom()), gap, imageSize);
    			
            	for (IFeature road : roadsG) {
                    ILineString geom = (ILineString) ((IMultiCurve<IOrientableCurve>) road.getGeom()).get(0);
                    IDirectPositionList viewDirectPositionList1 = transform.transform(geom.coord());
                    int numPoints1 = viewDirectPositionList1.size();
                    int[] xpoints1 = new int[numPoints1];
                    int[] ypoints1 = new int[numPoints1];
                    for (int i = 0; i < viewDirectPositionList1.size(); i++) {
                        IDirectPosition p3 = viewDirectPositionList1.get(i);
                        xpoints1[i] = (int) p3.getX();
                        ypoints1[i] = (int) p3.getY();
                    }
                    int nv=Integer.parseInt(((String)(road.getAttribute("nv").toString())))*2;
                    g2d.setColor(this.getInnerColor());
                    //g2d.setStroke(new BasicStroke((this.getInnerWidth())));
                    g2d.setStroke(new BasicStroke(nv));
                    g2d.drawPolyline(xpoints1, ypoints1, numPoints1);

                }
    			for (IFeature road : roadsI) {
                    ILineString geom = (ILineString) ((IMultiCurve<IOrientableCurve>) road.getGeom()).get(0);
                    IDirectPositionList viewDirectPositionList1 = transform.transform(geom.coord());
                    int numPoints1 = viewDirectPositionList1.size();
                    int[] xpoints1 = new int[numPoints1];
                    int[] ypoints1 = new int[numPoints1];
                    for (int i = 0; i < viewDirectPositionList1.size(); i++) {
                        IDirectPosition p3 = viewDirectPositionList1.get(i);
                        xpoints1[i] = (int) p3.getX()+imageSize;
                        ypoints1[i] = (int) p3.getY();
                    }
                    int nv=Integer.parseInt(((String)(road.getAttribute("nv").toString())))*2;
                    g2d.setColor(this.getInnerColor());
                    //g2d.setStroke(new BasicStroke((this.getInnerWidth())));
                    g2d.setStroke(new BasicStroke(nv));
                    g2d.drawPolyline(xpoints1, ypoints1, numPoints1);

                }

    			listImages.add(bi);
                }}
        		p1.setCoordinate(p1.getX(), p1.getY()+pas);
        		p2.setCoordinate(p2.getX(), p2.getY()+pas);
    		}
    		p1.setCoordinate(p1.getX()+pas, p1.getY());
    		p2.setCoordinate(p2.getX()+pas, p2.getY());
    	}

    	return listImages;
    	} 

    
	
	public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getCasingColor() {
        return casingColor;
    }

    public void setCasingColor(Color casingColor) {
        this.casingColor = casingColor;
    }

    public Color getInnerColor() {
        return innerColor;
    }

    public void setInnerColor(Color innerColor) {
        this.innerColor = innerColor;
    }

    public int getCasingWidth() {
        return casingWidth;
    }

    public void setCasingWidth(int casingWidth) {
        this.casingWidth = casingWidth;
    }

    public int getInnerWidth() {
        return innerWidth;
    }

    public void setInnerWidth(int innerWidth) {
        this.innerWidth = innerWidth;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
