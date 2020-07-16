package fr.acourtial.cartagen.plugins;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.xml.parsers.ParserConfigurationException;

//import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
//import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
//import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
//import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.store.ContentFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
//import org.nd4j.linalg.api.ndarray.INDArray;
//import org.nd4j.linalg.factory.Nd4j;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;
import org.xml.sax.SAXException;

import com.google.common.io.ByteStreams;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import fr.acourtial.cartagen.gui.CartAGenApplicationAgent;
import fr.ign.cogit.cartagen.algorithms.block.deletion.BuildingDeletionPromethee;
import fr.ign.cogit.cartagen.algorithms.block.deletion.BuildingsDeletionCongestion;
import fr.ign.cogit.cartagen.algorithms.block.deletion.BuildingsDeletionProximityMultiCriterion;
import fr.ign.cogit.cartagen.algorithms.block.displacement.BuildingDisplacementRandom;
import fr.ign.cogit.cartagen.appli.core.geoxygene.CartAGenPlugin;
import fr.ign.cogit.cartagen.appli.core.geoxygene.selection.SelectionUtil;
import fr.ign.cogit.cartagen.appli.plugins.machinelearning.TensorFlowPlugin;
import fr.ign.cogit.cartagen.core.GeneralisationSpecifications;
import fr.ign.cogit.cartagen.core.Legend;
import fr.ign.cogit.cartagen.core.SLDUtilCartagen;
import fr.ign.cogit.cartagen.core.dataset.CartAGenDataSet;
import fr.ign.cogit.cartagen.core.dataset.CartAGenDoc;
import fr.ign.cogit.cartagen.core.dataset.DefaultCartAGenDB;
import fr.ign.cogit.cartagen.core.dataset.DigitalLandscapeModel;
import fr.ign.cogit.cartagen.core.dataset.GeneObjImplementation;
import fr.ign.cogit.cartagen.core.dataset.SourceDLM;
import fr.ign.cogit.cartagen.core.dataset.json.JSONLoader;
import fr.ign.cogit.cartagen.core.dataset.json.JSONToLayerMapping;
import fr.ign.cogit.cartagen.core.dataset.postgis.MappingXMLParser;
import fr.ign.cogit.cartagen.core.defaultschema.urban.Town;
import fr.ign.cogit.cartagen.core.genericschema.AbstractCreationFactory;
import fr.ign.cogit.cartagen.core.genericschema.IGeneObj;
import fr.ign.cogit.cartagen.core.genericschema.urban.ITown;
import fr.ign.cogit.cartagen.core.genericschema.urban.IUrbanBlock;
import fr.ign.cogit.cartagen.core.genericschema.urban.IUrbanElement;
import fr.ign.cogit.cartagen.deeplearning.vector2image.Block2Image;
import fr.ign.cogit.cartagen.osm.schema.OsmGeneObj;
import fr.ign.cogit.cartagen.spatialanalysis.measures.BlockTriangulation;
import fr.ign.cogit.cartagen.spatialanalysis.network.NetworkEnrichment;
import fr.ign.cogit.cartagen.spatialanalysis.network.roads.RoadStructureDetection;
import fr.ign.cogit.cartagen.spatialanalysis.urban.BuildingClassifierSVM;
import fr.ign.cogit.cartagen.spatialanalysis.urban.BuildingClassifierSVM.BuildingClass;
import fr.ign.cogit.cartagen.spatialanalysis.urban.BuildingClassifierSVM.BuildingDescriptor;
import fr.ign.cogit.cartagen.spatialanalysis.urban.BuildingClassifierSVM.TrainingData;
import fr.ign.cogit.cartagen.spatialanalysis.urban.UrbanEnrichment;
import fr.ign.cogit.cartagen.util.multicriteriadecision.classifying.electretri.towncentre.BlockDensityCriterion;
import fr.ign.cogit.cartagen.util.multicriteriadecision.classifying.electretri.towncentre.BlockSizeCriterion;
import fr.ign.cogit.cartagen.util.multicriteriadecision.classifying.electretri.towncentre.BuildingHeightCriterion;
import fr.ign.cogit.cartagen.util.multicriteriadecision.classifying.electretri.towncentre.BuildingSizeCriterion;
import fr.ign.cogit.cartagen.util.multicriteriadecision.classifying.electretri.towncentre.CentroidCriterion;
import fr.ign.cogit.cartagen.util.multicriteriadecision.classifying.electretri.towncentre.ChurchCriterion;
import fr.ign.cogit.cartagen.util.multicriteriadecision.classifying.electretri.towncentre.LimitCriterion;
import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IPolygon;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.appli.GeOxygeneApplication;
import fr.ign.cogit.geoxygene.contrib.multicriteriadecision.Criterion;
import fr.ign.cogit.geoxygene.contrib.multicriteriadecision.classifying.ConclusionIntervals;
import fr.ign.cogit.geoxygene.contrib.multicriteriadecision.classifying.electretri.RobustELECTRETRIMethod;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.generalisation.simplification.SimplificationAlgorithm;
import fr.ign.cogit.geoxygene.style.FeatureTypeStyle;
import fr.ign.cogit.geoxygene.style.Layer;
import fr.ign.cogit.geoxygene.style.Style;
import fr.ign.cogit.geoxygene.style.StyledLayerDescriptor;
import fr.ign.cogit.geoxygene.util.CollectionsUtil;
import fr.ign.cogit.geoxygene.util.algo.CommonAlgorithms;
import fr.ign.cogit.geoxygene.util.conversion.AdapterFactory;
import fr.ign.cogit.geoxygene.util.conversion.JtsGeOxygene;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class PluginGuillaume extends JMenu {
//
//    private GeOxygeneApplication application;
//
//    /**
//     * 
//     */
//    private static final long serialVersionUID = 1L;
//
//    public PluginGuillaume(String title) {
//        super(title);
//        application = CartAGenPlugin.getInstance().getApplication();
//
//        JMenu menuTuto = new JMenu("Tutorials");
//        menuTuto.add(new JMenuItem(new AlgoTutoAction()));
//        this.add(menuTuto);
//        this.addSeparator();
//
//        JMenu menuLund = new JMenu("Lund dataset");
//        menuLund.add(new JMenuItem(new ExportShapeAction()));
//        this.add(menuLund);
//        this.addSeparator();
//
//        JMenu menuTensorFlow = new JMenu("TensorFlow");
//        menuTensorFlow.add(new JMenuItem(new LabelImageExampleAction()));
//        this.add(menuTensorFlow);
//        this.addSeparator();
//
//        JMenu menuMCD = new JMenu("Multiple Criteria");
//        JMenu menuElim = new JMenu("Building Elimination");
//        menuMCD.add(menuElim);
//        menuElim.add(new JMenuItem(new TestOrderDisplayAction()));
//        menuElim.add(new JMenuItem(new ShowElectreSelAction()));
//        menuElim.add(new JMenuItem(new ShowPrometheeSelAction()));
//        menuElim.add(new JMenuItem(new ShowAgentSelAction()));
//        menuElim.add(new JMenuItem(new CompareElimsAction()));
//        menuElim.add(new JMenuItem(new ClearSldAction()));
//        JMenu menuGraying = new JMenu("Block Graying");
//        menuGraying.add(new JMenuItem(new CompareBlocksAction()));
//        menuMCD.add(menuGraying);
//        this.add(menuMCD);
//        this.addSeparator();
//
//        this.add(new JMenuItem(new QuickTestAction()));
//    }
//
//    /**
//     * Action for quick tests, code is replaced each time a new test is carried
//     * out.
//     * 
//     * @author GTouya
//     * 
//     */
//    class QuickTestAction extends AbstractAction {
//
//        /**
//         * 
//         */
//        private static final long serialVersionUID = 1L;
//
//        @Override
//        public void actionPerformed(ActionEvent arg0) {
//
//            int truePos = 0, trueNeg = 0, falsePos = 0, falseNeg = 0;
//
//            File dirPositive = new File(
//                    "D:\\MySyncFolder\\deep\\deep_interchange\\images_and_jsons_for_classif\\jsons\\val\\1");
//            File dirNegative = new File(
//                    "D:\\MySyncFolder\\deep\\deep_interchange\\images_and_jsons_for_classif\\jsons\\val\\0");
//            File mappingFile = new File(
//                    "D:\\workspace\\gtouya-CartAGen\\src\\main\\resources\\xml\\mapping_json_deep.xml");
//
//            for (File file : dirPositive.listFiles()) {
//                if (file.isDirectory())
//                    continue;
//
//                CartAGenDoc doc = CartAGenDoc.getInstance();
//                doc.setName("default");
//                // load the data
//                DefaultCartAGenDB database = new DefaultCartAGenDB("default");
//                database.setSymboScale(15000);
//                database.setGeneObjImpl(
//                        GeneObjImplementation.getDefaultImplementation());
//                CartAGenDataSet dataset = new CartAGenDataSet();
//                doc.addDatabase("default", database);
//                database.setDataSet(dataset);
//                database.setType(new DigitalLandscapeModel());
//
//                try {
//                    JSONToLayerMapping mapping = new MappingXMLParser(
//                            mappingFile).parseJSONMapping();
//                    AbstractCreationFactory factory = mapping
//                            .getGeneObjImplementation().getCreationFactory();
//                    JSONLoader loader = new JSONLoader();
//                    loader.genericJSONLoader(file.getPath(),
//                            doc.getCurrentDataset(), factory, mapping, true);
//                    System.out.println(database.getDataSet().getRoads().size()
//                            + " loaded roads");
//
//                } catch (ParserConfigurationException | SAXException
//                        | IOException | NoSuchFieldException | SecurityException
//                        | InvocationTargetException e) {
//                    e.printStackTrace();
//                }
//                // enrich the data
//                NetworkEnrichment.enrichNetwork(
//                        CartAGenDoc.getInstance().getCurrentDataset(),
//                        CartAGenDoc.getInstance().getCurrentDataset()
//                                .getRoadNetwork(),
//                        CartAGenDoc.getInstance().getCurrentDataset()
//                                .getCartAGenDB().getGeneObjImpl()
//                                .getCreationFactory());
//                // detect the interchanges
//                RoadStructureDetection detect = new RoadStructureDetection();
//                Collection<IPolygon> interchanges = detect.detectInterchanges(2,
//                        true);
//                if (interchanges.size() > 0)
//                    truePos++;
//                else
//                    falseNeg++;
//            }
//
//            for (File file : dirNegative.listFiles()) {
//                if (file.isDirectory())
//                    continue;
//
//                CartAGenDoc doc = CartAGenDoc.getInstance();
//                doc.setName("default");
//                // load the data
//                DefaultCartAGenDB database = new DefaultCartAGenDB("default");
//                database.setSymboScale(15000);
//                database.setGeneObjImpl(
//                        GeneObjImplementation.getDefaultImplementation());
//                CartAGenDataSet dataset = new CartAGenDataSet();
//                doc.addDatabase("default", database);
//                database.setDataSet(dataset);
//                database.setType(new DigitalLandscapeModel());
//
//                try {
//                    JSONToLayerMapping mapping = new MappingXMLParser(
//                            mappingFile).parseJSONMapping();
//                    AbstractCreationFactory factory = mapping
//                            .getGeneObjImplementation().getCreationFactory();
//                    JSONLoader loader = new JSONLoader();
//                    loader.genericJSONLoader(file.getPath(),
//                            doc.getCurrentDataset(), factory, mapping, true);
//                    System.out.println(database.getDataSet().getRoads().size()
//                            + " loaded roads");
//
//                } catch (ParserConfigurationException | SAXException
//                        | IOException | NoSuchFieldException | SecurityException
//                        | InvocationTargetException e) {
//                    e.printStackTrace();
//                }
//                // enrich the data
//                NetworkEnrichment.enrichNetwork(
//                        CartAGenDoc.getInstance().getCurrentDataset(),
//                        CartAGenDoc.getInstance().getCurrentDataset()
//                                .getRoadNetwork(),
//                        CartAGenDoc.getInstance().getCurrentDataset()
//                                .getCartAGenDB().getGeneObjImpl()
//                                .getCreationFactory());
//                // detect the interchanges
//                RoadStructureDetection detect = new RoadStructureDetection();
//                Collection<IPolygon> interchanges = detect.detectInterchanges(2,
//                        true);
//
//                if (interchanges.size() > 0)
//                    falsePos++;
//                else
//                    trueNeg++;
//            }
//
//            System.out.println("true positive: " + truePos);
//            System.out.println("true negative: " + trueNeg);
//            System.out.println("false positive: " + falsePos);
//            System.out.println("false negative: " + falseNeg);
//        }
//
//        public QuickTestAction() {
//            this.putValue(Action.SHORT_DESCRIPTION,
//                    "Quick test of a misc code");
//            this.putValue(Action.NAME, "Quick Test");
//        }
//    }
//
//    /**
//     * Identifies the roundabouts in the selected road layer, and creates a new
//     * layer with the roundabouts.
//     * 
//     * @author GTouya
//     * 
//     */
//    class AlgoTutoAction extends AbstractAction {
//
//        /****/
//        private static final long serialVersionUID = 1L;
//
//        @Override
//        public void actionPerformed(ActionEvent arg0) {
//            CartAGenDataSet dataset = CartAGenDoc.getInstance()
//                    .getCurrentDataset();
//            StyledLayerDescriptor sld = CartAGenDoc.getInstance()
//                    .getCurrentDataset().getSld();
//            SLDUtilCartagen.changeSymbolisationScale(30000.0, sld);
//
//            // get the network sections that delimit groups (i.e. roads and
//            // rivers)
//            IFeatureCollection<IFeature> sections = new FT_FeatureCollection<>();
//            sections.addAll(dataset.getRoads());
//            sections.addAll(dataset.getWaterLines());
//
//            // create the groups with a 25.0 m threshold for building buffering
//            Collection<IUrbanBlock> groups = UrbanEnrichment
//                    .createBuildingGroups(sections, 25.0, 10.0, 12, 2.0,
//                            5000.0);
//
//            for (IUrbanBlock group : groups) {
//
//                for (IUrbanElement building : group.getUrbanElements()) {
//                    // in this case, we only added buildings as urban elements,
//                    // so no need
//                    // to check (there can be parks, squares, sports fields...)
//                    // the class GeneralisationSpecifications is used: it
//                    // contains
//                    // standard values for classical generalisation
//                    // specifications, such
//                    // as the minimum size of a building in map mm²
//
//                    // first enlarge the building geometry
//                    // compute the goal area
//                    double area = building.getGeom().area();
//                    double goalArea = area;
//                    double aireMini = GeneralisationSpecifications.BUILDING_MIN_AREA
//                            * Legend.getSYMBOLISATI0N_SCALE()
//                            * Legend.getSYMBOLISATI0N_SCALE() / 1000000.0;
//                    if (area <= aireMini) {
//                        goalArea = aireMini;
//                    }
//                    // compute the homothety of the building geometry
//                    IPolygon geom = CommonAlgorithms.homothetie(
//                            (IPolygon) building.getGeom(),
//                            Math.sqrt(goalArea / area));
//
//                    // then simplify the building
//                    IGeometry simplified = SimplificationAlgorithm
//                            .simplification(geom,
//                                    GeneralisationSpecifications.LONGUEUR_MINI_GRANULARITE
//                                            * Legend.getSYMBOLISATI0N_SCALE()
//                                            / 1000.0);
//
//                    // apply the new geometry to the building
//                    building.setGeom(simplified);
//                }
//
//                // trigger the displacement of the enlarged and simplified
//                // features
//                BuildingDisplacementRandom.compute(group);
//            }
//        }
//
//        public AlgoTutoAction() {
//            this.putValue(Action.NAME, "Algorithms tutorial");
//        }
//    }
//
//    class ExportShapeAction extends AbstractAction {
//
//        /**
//         * 
//         */
//        private static final long serialVersionUID = 1L;
//
//        @Override
//        public void actionPerformed(ActionEvent arg0) {
//            Class<? extends IGeometry> geomType = ILineString.class;
//
//            // get the features to export
//            Collection<? extends IFeature> iterable = CartAGenDoc.getInstance()
//                    .getCurrentDataset().getRoads();
//
//            IFeatureCollection<IFeature> features = new FT_FeatureCollection<IFeature>();
//            for (IFeature obj : iterable) {
//                if (!(obj instanceof IGeneObj)) {
//                    features.add(obj);
//                    continue;
//                }
//                if (!((IGeneObj) obj).isEliminated()) {
//                    features.add((IGeneObj) obj);
//                }
//            }
//
//            // write the shapefile
//            write(features, geomType,
//                    "D:\\Donnees\\OSM\\Lund_workshop\\derived layers\\roads.shp",
//                    "roads");
//        }
//
//        public ExportShapeAction() {
//            this.putValue(Action.SHORT_DESCRIPTION,
//                    "Export roads to shapefile with all tags");
//            this.putValue(Action.NAME, "Export roads to shapefile");
//        }
//
//        @SuppressWarnings({ "unchecked", "rawtypes" })
//        public <Feature extends IFeature> void write(
//                IFeatureCollection<IFeature> featurePop,
//                Class<? extends IGeometry> geomType, String shpName,
//                String layerName) {
//            if (featurePop == null) {
//                return;
//            }
//            if (featurePop.isEmpty()) {
//                return;
//            }
//            String shapefileName = shpName;
//            try {
//                if (!shapefileName.contains(".shp")) { //$NON-NLS-1$
//                    shapefileName = shapefileName + ".shp"; //$NON-NLS-1$
//                }
//                ShapefileDataStore store = new ShapefileDataStore(
//                        new File(shapefileName).toURI().toURL());
//
//                // specify the geometry type
//                String specs = "the_geom:"; //$NON-NLS-1$
//                specs += "LineString";
//
//                // specify the attributes: there is only one the MRDB link
//                specs += "," + "osm_id" + ":" + "String";
//                specs += "," + "fclass" + ":" + "String";
//                specs += "," + "name" + ":" + "String";
//
//                SimpleFeatureType type = DataUtilities.createType(layerName,
//                        specs);
//                store.createSchema(type);
//                ContentFeatureStore featureStore = (ContentFeatureStore) store
//                        .getFeatureSource(layerName);
//                Transaction t = new DefaultTransaction();
//                Collection features = new HashSet<>();
//                int i = 1;
//                for (IFeature feature : featurePop) {
//                    if (feature.isDeleted()) {
//                        continue;
//                    }
//                    List<Object> liste = new ArrayList<Object>(0);
//                    // change the CRS if needed
//                    IGeometry geom = feature.getGeom();
//                    if ((geom instanceof ILineString)
//                            && (geom.coord().size() < 2))
//                        continue;
//
//                    liste.add(AdapterFactory.toGeometry(new GeometryFactory(),
//                            geom));
//                    // liste.add(feature.getId());
//                    // put the attributes in the list, after the geometry
//                    liste.add(
//                            String.valueOf(((OsmGeneObj) feature).getOsmId()));
//                    liste.add(String.valueOf(
//                            ((OsmGeneObj) feature).getTags().get("highway")));
//                    liste.add(String.valueOf(
//                            ((OsmGeneObj) feature).getTags().get("name")));
//                    SimpleFeature simpleFeature = SimpleFeatureBuilder
//                            .build(type, liste.toArray(), String.valueOf(i++));
//                    System.out.println(i);
//                    System.out.println(liste);
//                    features.add(simpleFeature);
//                }
//                featureStore.addFeatures(features);
//                t.commit();
//                t.close();
//                store.dispose();
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (SchemaException e) {
//                e.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//
//    /**
//     * Test the display of the order in a list.
//     * 
//     * @author GTouya
//     * 
//     */
//    class TestOrderDisplayAction extends AbstractAction {
//
//        /**
//         * 
//         */
//        private static final long serialVersionUID = 1L;
//
//        @Override
//        public void actionPerformed(ActionEvent arg0) {
//
//            List<IFeature> orderedFeatures = SelectionUtil
//                    .getListOfSelectedObjects(application);
//            StyledLayerDescriptor sld = application.getMainFrame()
//                    .getSelectedProjectFrame().getSld();
//
//            SLDUtilCartagen.addOrderTextSymbolizer(sld, orderedFeatures,
//                    "buildings", Color.BLACK, 15);
//            application.getMainFrame().getSelectedProjectFrame().repaint();
//        }
//
//        public TestOrderDisplayAction() {
//            this.putValue(Action.SHORT_DESCRIPTION,
//                    "Test of order display with the selected buildings");
//            this.putValue(Action.NAME, "Test order display");
//        }
//    }
//
//    /**
//     * Show ELECTRE selection on the selected blocks by displaying the selection
//     * order.
//     * 
//     * @author GTouya
//     * 
//     */
//    class ShowElectreSelAction extends AbstractAction {
//
//        /**
//         * 
//         */
//        private static final long serialVersionUID = 1L;
//
//        @Override
//        public void actionPerformed(ActionEvent arg0) {
//
//            List<IFeature> selectedBlocks = SelectionUtil
//                    .getListOfSelectedObjects(application);
//            StyledLayerDescriptor sld = application.getMainFrame()
//                    .getSelectedProjectFrame().getSld();
//
//            for (IFeature feat : selectedBlocks) {
//                List<IFeature> orderedFeatures = new ArrayList<>();
//                orderedFeatures.addAll(BuildingsDeletionProximityMultiCriterion
//                        .compute((IUrbanBlock) feat));
//                SLDUtilCartagen.addOrderTextSymbolizer(sld, orderedFeatures,
//                        "buildings", Color.BLACK, 15);
//            }
//            application.getMainFrame().getSelectedProjectFrame().repaint();
//        }
//
//        public ShowElectreSelAction() {
//            this.putValue(Action.SHORT_DESCRIPTION,
//                    "Show ELECTRE selection on the selected blocks");
//            this.putValue(Action.NAME, "Show ELECTRE selection");
//        }
//    }
//
//    /**
//     * Show PROMETHEE selection on the selected blocks by displaying the
//     * selection order.
//     * 
//     * @author GTouya
//     * 
//     */
//    class ShowPrometheeSelAction extends AbstractAction {
//
//        /**
//         * 
//         */
//        private static final long serialVersionUID = 1L;
//
//        @Override
//        public void actionPerformed(ActionEvent arg0) {
//
//            List<IFeature> selectedBlocks = SelectionUtil
//                    .getListOfSelectedObjects(application);
//            StyledLayerDescriptor sld = application.getMainFrame()
//                    .getSelectedProjectFrame().getSld();
//
//            for (IFeature feat : selectedBlocks) {
//                BlockTriangulation.buildTriangulation((IUrbanBlock) feat,
//                        GeneralisationSpecifications.DISTANCE_MAX_PROXIMITE);
//                List<IFeature> orderedFeatures = new ArrayList<>();
//                BuildingDeletionPromethee process = new BuildingDeletionPromethee();
//                orderedFeatures.addAll(process.compute((IUrbanBlock) feat));
//                SLDUtilCartagen.addOrderTextSymbolizer(sld, orderedFeatures,
//                        "buildings", Color.BLACK, 15);
//            }
//            application.getMainFrame().getSelectedProjectFrame().repaint();
//        }
//
//        public ShowPrometheeSelAction() {
//            this.putValue(Action.SHORT_DESCRIPTION,
//                    "Show PROMETHEE selection on the selected blocks");
//            this.putValue(Action.NAME, "Show PROMETHEE selection");
//        }
//    }
//
//    /**
//     * Show AGENT selection on the selected blocks by displaying the selection
//     * order.
//     * 
//     * @author GTouya
//     * 
//     */
//    class ShowAgentSelAction extends AbstractAction {
//
//        /**
//         * 
//         */
//        private static final long serialVersionUID = 1L;
//
//        @Override
//        public void actionPerformed(ActionEvent arg0) {
//
//            List<IFeature> selectedBlocks = SelectionUtil
//                    .getListOfSelectedObjects(application);
//            StyledLayerDescriptor sld = application.getMainFrame()
//                    .getSelectedProjectFrame().getSld();
//
//            for (IFeature feat : selectedBlocks) {
//                List<IFeature> orderedFeatures = new ArrayList<>();
//                orderedFeatures.addAll(BuildingsDeletionCongestion.compute(
//                        (IUrbanBlock) feat, 0,
//                        GeneralisationSpecifications.DISTANCE_MAX_PROXIMITE,
//                        0.0));
//                for (IFeature building : orderedFeatures)
//                    building.setDeleted(false);
//                SLDUtilCartagen.addOrderTextSymbolizer(sld, orderedFeatures,
//                        "buildings", Color.BLACK, 15);
//            }
//            application.getMainFrame().getSelectedProjectFrame().repaint();
//        }
//
//        public ShowAgentSelAction() {
//            this.putValue(Action.SHORT_DESCRIPTION,
//                    "Show AGENT selection on the selected blocks");
//            this.putValue(Action.NAME, "Show AGENT selection");
//        }
//    }
//
//    /**
//     * Test the display of the order in a list.
//     * 
//     * @author GTouya
//     * 
//     */
//    class ClearSldAction extends AbstractAction {
//
//        /**
//         * 
//         */
//        private static final long serialVersionUID = 1L;
//
//        @Override
//        public void actionPerformed(ActionEvent arg0) {
//
//            StyledLayerDescriptor sld = application.getMainFrame()
//                    .getSelectedProjectFrame().getSld();
//            Layer layer = sld.getLayer("buildings");
//            Style style = layer.getStyles().get(0);
//            FeatureTypeStyle orderStyle = null;
//            for (FeatureTypeStyle fts : style.getFeatureTypeStyles()) {
//                if ("text order".equals(fts.getName())) {
//                    orderStyle = fts;
//                    break;
//                }
//            }
//            if (orderStyle != null)
//                style.getFeatureTypeStyles().remove(orderStyle);
//
//            application.getMainFrame().getSelectedProjectFrame().repaint();
//        }
//
//        public ClearSldAction() {
//            this.putValue(Action.NAME, "Clear SLD from order display");
//        }
//    }
//
//    private class LabelImageExampleAction extends AbstractAction {
//
//        private static final long serialVersionUID = 1L;
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            Thread th = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    File[] files = null;
//                    JFileChooser jfc = new JFileChooser();
//                    jfc.setMultiSelectionEnabled(true);
//                    int returnVal = jfc.showOpenDialog(
//                            application.getMainFrame().getGui());
//                    if (returnVal == JFileChooser.APPROVE_OPTION) {
//                        files = jfc.getSelectedFiles();
//                    }
//
//                    List<String> labels = new ArrayList<>();
//                    try {
//                        labels = loadLabels();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    try (Graph graph = new Graph();
//                            Session session = new Session(graph)) {
//                        graph.importGraphDef(loadGraphDef());
//
//                        float[] probabilities = null;
//                        for (File file : files) {
//                            byte[] bytes = Files
//                                    .readAllBytes(Paths.get(file.toURI()));
//                            try (Tensor<String> input = Tensors.create(bytes);
//                                    Tensor<Float> output = session.runner()
//                                            .feed("encoded_image_bytes", input)
//                                            .fetch("probabilities").run().get(0)
//                                            .expect(Float.class)) {
//                                if (probabilities == null) {
//                                    probabilities = new float[(int) output
//                                            .shape()[0]];
//                                }
//                                output.copyTo(probabilities);
//                                int label = argmax(probabilities);
//                                System.out.printf(
//                                        "%-30s --> %-15s (%.2f%% likely)\n",
//                                        file.getPath(), labels.get(label),
//                                        probabilities[label] * 100.0);
//                            }
//                        }
//                    } catch (IllegalArgumentException | IOException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            });
//            th.start();
//        }
//
//        private byte[] loadGraphDef() throws IOException {
//            try (InputStream is = TensorFlowPlugin.class.getClassLoader()
//                    .getResourceAsStream("tensorflow/inception5h/graph.pb")) {
//                return ByteStreams.toByteArray(is);
//            }
//        }
//
//        private ArrayList<String> loadLabels() throws IOException {
//            ArrayList<String> labels = new ArrayList<String>();
//            String line;
//            final InputStream is = TensorFlowPlugin.class.getClassLoader()
//                    .getResourceAsStream("tensorflow/inception5h/labels.txt");
//            try (BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(is))) {
//                while ((line = reader.readLine()) != null) {
//                    labels.add(line);
//                }
//            }
//            return labels;
//        }
//
//        private int argmax(float[] probabilities) {
//            int best = 0;
//            for (int i = 1; i < probabilities.length; ++i) {
//                if (probabilities[i] > probabilities[best]) {
//                    best = i;
//                }
//            }
//            return best;
//        }
//
//        public LabelImageExampleAction() {
//            super();
//            this.putValue(Action.NAME, "LabelImage TF example");
//        }
//    }
//
//    /**
//     * Compare building elimination methods on the selected blocks and store the
//     * comparisons in Excel sheet.
//     * 
//     * @author GTouya
//     * 
//     */
//    class CompareElimsAction extends AbstractAction {
//
//        /**
//         * 
//         */
//        private static final long serialVersionUID = 1L;
//
//        @Override
//        public void actionPerformed(ActionEvent arg0) {
//
//            List<IFeature> selectedBlocks = SelectionUtil
//                    .getListOfSelectedObjects(application);
//
//            JFileChooser fc = new JFileChooser();
//            int returnVal = fc.showSaveDialog(CartAGenPlugin.getInstance()
//                    .getApplication().getMainFrame().getGui());
//            if (returnVal != JFileChooser.APPROVE_OPTION) {
//                return;
//            }
//            File file = fc.getSelectedFile();
//            // create an excel sheet to store the results
//            try {
//                WritableWorkbook workbook = Workbook.createWorkbook(file);
//                WritableSheet sheet = workbook
//                        .createSheet("elimination_comparison", 1);
//
//                // write the column headers
//                sheet.addCell(new Label(0, 0, "block id"));
//                sheet.addCell(new Label(1, 0, "electre list"));
//                sheet.addCell(new Label(2, 0, "promethee list"));
//                sheet.addCell(new Label(3, 0, "agent list"));
//                sheet.addCell(new Label(4, 0, "hamming_prom"));
//                sheet.addCell(new Label(5, 0, "hamming_agent"));
//                sheet.addCell(new Label(6, 0, "hamming_prom_5"));
//                sheet.addCell(new Label(7, 0, "hamming_agent_5"));
//                sheet.addCell(new Label(8, 0, "edit_prom"));
//                sheet.addCell(new Label(9, 0, "edit_agent"));
//                sheet.addCell(new Label(10, 0, "edit_prom_5"));
//                sheet.addCell(new Label(11, 0, "edit_agent_5"));
//                sheet.addCell(new Label(12, 0, "jaro_prom"));
//                sheet.addCell(new Label(13, 0, "jaro_agent"));
//                sheet.addCell(new Label(14, 0, "jaro_prom_5"));
//                sheet.addCell(new Label(15, 0, "jaro_agent_5"));
//                sheet.addCell(new Label(16, 0, "norm_hamming_prom"));
//                sheet.addCell(new Label(17, 0, "norm_hamming_agent"));
//                sheet.addCell(new Label(18, 0, "norm_hamming_prom_5"));
//                sheet.addCell(new Label(19, 0, "norm_hamming_agent_5"));
//                sheet.addCell(new Label(20, 0, "norm_edit_prom"));
//                sheet.addCell(new Label(21, 0, "norm_edit_agent"));
//                sheet.addCell(new Label(22, 0, "norm_edit_prom_5"));
//                sheet.addCell(new Label(23, 0, "norm_edit_agent_5"));
//                sheet.addCell(new Label(24, 0, "nb_buildings"));
//                sheet.addCell(new Label(25, 0, "hamming_prom_agent"));
//                sheet.addCell(new Label(26, 0, "norm_hamming_prom_agent"));
//                sheet.addCell(new Label(27, 0, "edit_prom_agent"));
//                sheet.addCell(new Label(28, 0, "norm_edit_prom_agent"));
//                sheet.addCell(new Label(29, 0, "jaro_prom_agent"));
//                sheet.addCell(new Label(30, 0, "norm_jaro_prom_agent"));
//
//                int i = 1;
//                for (IFeature feat : selectedBlocks) {
//
//                    if (((IUrbanBlock) feat).getUrbanElements().size() == 0)
//                        continue;
//
//                    // compute AGENT ordering
//                    List<IFeature> agentOrdering = new ArrayList<>();
//                    agentOrdering.addAll(BuildingsDeletionCongestion.compute(
//                            (IUrbanBlock) feat, 0,
//                            GeneralisationSpecifications.DISTANCE_MAX_PROXIMITE,
//                            0.0));
//                    for (IFeature building : agentOrdering)
//                        building.setDeleted(false);
//                    List<IFeature> agentOrdering5 = new ArrayList<>();
//
//                    if (agentOrdering.size() < 5)
//                        agentOrdering5.addAll(agentOrdering);
//                    else {
//                        for (int j = 0; j < 5; j++)
//                            agentOrdering5.add(agentOrdering.get(j));
//                    }
//
//                    // compute PROMETHEE ordering
//                    BlockTriangulation.buildTriangulation((IUrbanBlock) feat,
//                            GeneralisationSpecifications.DISTANCE_MAX_PROXIMITE);
//                    List<IFeature> prometheeOrder = new ArrayList<>();
//                    BuildingDeletionPromethee process = new BuildingDeletionPromethee();
//                    prometheeOrder.addAll(process.compute((IUrbanBlock) feat));
//                    List<IFeature> prometheeOrder5 = new ArrayList<>();
//                    if (prometheeOrder.size() < 5)
//                        prometheeOrder5.addAll(prometheeOrder);
//                    else {
//                        for (int j = 0; j < 5; j++)
//                            prometheeOrder5.add(prometheeOrder.get(j));
//                    }
//
//                    // compute ELECTRE ordering
//                    List<IFeature> electreOrder = new ArrayList<>();
//                    electreOrder.addAll(BuildingsDeletionProximityMultiCriterion
//                            .compute((IUrbanBlock) feat));
//                    List<IFeature> electreOrder5 = new ArrayList<>();
//                    if (electreOrder.size() < 5)
//                        electreOrder5.addAll(electreOrder);
//                    else {
//                        for (int j = 0; j < 5; j++)
//                            electreOrder5.add(electreOrder.get(j));
//                    }
//
//                    // compute the distances
//                    double hammingElecProm = CollectionsUtil
//                            .getHammingDistance2Lists(electreOrder,
//                                    prometheeOrder);
//                    double hammingElecAgent = CollectionsUtil
//                            .getHammingDistance2DiffLists(electreOrder,
//                                    agentOrdering);
//                    double hammingElecProm5 = CollectionsUtil
//                            .getHammingDistance2Lists(electreOrder5,
//                                    prometheeOrder5);
//                    double hammingElecAgent5 = CollectionsUtil
//                            .getHammingDistance2DiffLists(electreOrder5,
//                                    agentOrdering5);
//                    double hammingPromAgent = CollectionsUtil
//                            .getHammingDistance2Lists(agentOrdering,
//                                    prometheeOrder);
//                    double editElecProm = CollectionsUtil.getEditDistance2Lists(
//                            electreOrder, prometheeOrder);
//                    double editElecAgent = CollectionsUtil
//                            .getEditDistance2Lists(electreOrder, agentOrdering);
//                    double editElecProm5 = CollectionsUtil
//                            .getEditDistance2Lists(electreOrder5,
//                                    prometheeOrder5);
//                    double editElecAgent5 = CollectionsUtil
//                            .getEditDistance2Lists(electreOrder5,
//                                    agentOrdering5);
//                    double editPromAgent = CollectionsUtil
//                            .getEditDistance2Lists(prometheeOrder,
//                                    agentOrdering);
//                    double jaroElecProm = CollectionsUtil
//                            .getJaroWinklerDistance2Lists(electreOrder,
//                                    prometheeOrder, 0.1);
//                    double jaroElecAgent = CollectionsUtil
//                            .getJaroWinklerDistance2Lists(electreOrder,
//                                    agentOrdering, 0.1);
//                    double jaroElecProm5 = CollectionsUtil
//                            .getJaroWinklerDistance2Lists(electreOrder5,
//                                    prometheeOrder5, 0.1);
//                    double jaroElecAgent5 = CollectionsUtil
//                            .getJaroWinklerDistance2Lists(electreOrder5,
//                                    agentOrdering5, 0.1);
//                    double jaroPromAgent = CollectionsUtil
//                            .getJaroWinklerDistance2Lists(agentOrdering,
//                                    prometheeOrder, 0.1);
//
//                    // write a new line in the excel sheet
//                    sheet.addCell(
//                            new Label(0, i, String.valueOf(feat.getId())));
//                    sheet.addCell(new Label(1, i, listToString(electreOrder)));
//                    sheet.addCell(
//                            new Label(2, i, listToString(prometheeOrder)));
//                    sheet.addCell(new Label(3, i, listToString(agentOrdering)));
//                    sheet.addCell(new jxl.write.Number(4, i, hammingElecProm));
//                    sheet.addCell(new jxl.write.Number(5, i, hammingElecAgent));
//                    sheet.addCell(new jxl.write.Number(6, i, hammingElecProm5));
//                    sheet.addCell(
//                            new jxl.write.Number(7, i, hammingElecAgent5));
//                    sheet.addCell(new jxl.write.Number(8, i, editElecProm));
//                    sheet.addCell(new jxl.write.Number(9, i, editElecAgent));
//                    sheet.addCell(new jxl.write.Number(10, i, editElecProm5));
//                    sheet.addCell(new jxl.write.Number(11, i, editElecAgent5));
//                    sheet.addCell(new jxl.write.Number(12, i, jaroElecProm));
//                    sheet.addCell(new jxl.write.Number(13, i, jaroElecAgent));
//                    sheet.addCell(new jxl.write.Number(14, i, jaroElecProm5));
//                    sheet.addCell(new jxl.write.Number(15, i, jaroElecAgent5));
//                    sheet.addCell(new jxl.write.Number(16, i, Math.max(1,
//                            hammingElecProm / electreOrder.size())));
//                    sheet.addCell(new jxl.write.Number(17, i, Math.max(1,
//                            hammingElecAgent / electreOrder.size())));
//                    sheet.addCell(new jxl.write.Number(18, i, Math.max(1,
//                            hammingElecProm5 / electreOrder.size())));
//                    sheet.addCell(new jxl.write.Number(19, i, Math.max(1,
//                            hammingElecAgent5 / electreOrder.size())));
//                    sheet.addCell(new jxl.write.Number(20, i,
//                            Math.max(1, editElecProm / electreOrder.size())));
//                    sheet.addCell(new jxl.write.Number(21, i,
//                            Math.max(1, editElecAgent / electreOrder.size())));
//                    sheet.addCell(new jxl.write.Number(22, i,
//                            Math.max(1, editElecProm5 / electreOrder.size())));
//                    sheet.addCell(new jxl.write.Number(23, i,
//                            Math.max(1, editElecAgent5 / electreOrder.size())));
//                    sheet.addCell(
//                            new jxl.write.Number(24, i, electreOrder.size()));
//                    sheet.addCell(
//                            new jxl.write.Number(25, i, hammingPromAgent));
//                    sheet.addCell(new jxl.write.Number(26, i, Math.max(1,
//                            hammingPromAgent / electreOrder.size())));
//                    sheet.addCell(new jxl.write.Number(27, i, editPromAgent));
//                    sheet.addCell(new jxl.write.Number(28, i,
//                            Math.max(1, editPromAgent / electreOrder.size())));
//                    sheet.addCell(new jxl.write.Number(29, i, jaroPromAgent));
//                    sheet.addCell(new jxl.write.Number(30, i,
//                            Math.max(1, jaroPromAgent / electreOrder.size())));
//                    i++;
//                }
//
//                workbook.write();
//                workbook.close();
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            } catch (WriteException e1) {
//                e1.printStackTrace();
//            }
//        }
//
//        public CompareElimsAction() {
//            this.putValue(Action.SHORT_DESCRIPTION,
//                    "Compare methods on the selected blocks");
//            this.putValue(Action.NAME, "Compare methods");
//        }
//
//        private String listToString(List<IFeature> list) {
//            if (list.isEmpty())
//                return "";
//            StringBuffer output = new StringBuffer();
//            output.append("[");
//            for (IFeature feat : list) {
//                output.append(feat.getId());
//                output.append(",");
//            }
//            output.deleteCharAt(output.length() - 1);
//            output.append("]");
//            return output.toString();
//        }
//    }
//
//    /**
//     * Compare block classification methods on the selected blocks and store the
//     * comparisons in Excel sheet.
//     * 
//     * @author GTouya
//     * 
//     */
//    class CompareBlocksAction extends AbstractAction {
//
//        /**
//         * 
//         */
//        private static final long serialVersionUID = 1L;
//
//        @Override
//        public void actionPerformed(ActionEvent arg0) {
//
//            List<IFeature> selectedBlocks = SelectionUtil
//                    .getListOfSelectedObjects(application);
//            System.out.println(selectedBlocks.size() + " blocks selected");
//
//            JFileChooser fc = new JFileChooser();
//            int returnVal = fc.showSaveDialog(CartAGenPlugin.getInstance()
//                    .getApplication().getMainFrame().getGui());
//            if (returnVal != JFileChooser.APPROVE_OPTION) {
//                return;
//            }
//            File file = fc.getSelectedFile();
//
//            // train the SVM
//            List<BuildingDescriptor> descriptorsUsed = new ArrayList<>();
//            descriptorsUsed.add(BuildingDescriptor.valueOf("BAr"));
//            descriptorsUsed.add(BuildingDescriptor.valueOf("BCo"));
//            descriptorsUsed.add(BuildingDescriptor.valueOf("BSh"));
//            descriptorsUsed.add(BuildingDescriptor.valueOf("BSq"));
//            descriptorsUsed.add(BuildingDescriptor.valueOf("BEl"));
//            descriptorsUsed.add(BuildingDescriptor.valueOf("BCy"));
//            descriptorsUsed.add(BuildingDescriptor.valueOf("NoBdg"));
//            descriptorsUsed.add(BuildingDescriptor.valueOf("BAHull"));
//            descriptorsUsed.add(BuildingDescriptor.valueOf("BABuff"));
//            TrainingData training = null;
//            BuildingClassifierSVM svm = new BuildingClassifierSVM(
//                    new FT_FeatureCollection<IFeature>());
//            try {
//                training = svm.new TrainingData(new File(
//                        "D:\\workspace\\gtouya-CartAGen\\src\\main\\resources\\xml\\training\\training_set_65_25k.xml"),
//                        descriptorsUsed);
//            } catch (ParserConfigurationException | SAXException
//                    | IOException e2) {
//                e2.printStackTrace();
//            }
//
//            svm.train(training);
//            System.out.println("SVM trained");
//
//            // create an excel sheet to store the results
//            try {
//                WritableWorkbook workbook = Workbook.createWorkbook(file);
//                WritableSheet sheet = workbook
//                        .createSheet("block_classif_comparison", 1);
//
//                // write the column headers
//                sheet.addCell(new Label(0, 0, "block id"));
//                sheet.addCell(new Label(1, 0, "electre class"));
//                sheet.addCell(new Label(2, 0, "svm clss"));
//                sheet.addCell(new Label(3, 0, "deep class"));
//
//                int i = 1;
//                for (IFeature feat : selectedBlocks) {
//
//                    if (((IUrbanBlock) feat).getTown() == null) {
//                        System.out.println("no town");
//                        continue;
//                    }
//
//                    ITown town = ((IUrbanBlock) feat).getTown();
//
//                    // ******************** compute ELECTRE classification
//                    // compute the statistics on the town blocks
//                    if (((Town) town).getMeanBlockArea() == null) {
//                        ((Town) town).computeTownStats();
//                    }
//
//                    // build the decision method and its criteria
//                    RobustELECTRETRIMethod electre = new RobustELECTRETRIMethod();
//                    Set<Criterion> criteria = new HashSet<Criterion>();
//                    boolean special = false;
//                    if (SourceDLM.SPECIAL_CARTAGEN.equals(
//                            CartAGenDoc.getInstance().getCurrentDataset()
//                                    .getCartAGenDB().getSourceDLM()))
//                        special = true;
//                    if (!special)
//                        criteria.add(new ChurchCriterion("Church"));
//                    criteria.add(new BlockDensityCriterion("Density"));
//                    criteria.add(new BlockSizeCriterion("Area"));
//                    if (!special)
//                        criteria.add(new BuildingHeightCriterion("Height"));
//                    criteria.add(new CentroidCriterion("Centroid"));
//                    criteria.add(new BuildingSizeCriterion("BuildingArea"));
//                    criteria.add(new LimitCriterion("Limit"));
//                    ConclusionIntervals conclusion = this
//                            .initCentreConclusion(criteria);
//                    electre.setCriteriaParamsFromCriteria(criteria);
//
//                    // make the decision
//                    Map<String, Double> valeursCourantes = new HashMap<String, Double>();
//                    for (Criterion crit : criteria) {
//                        Map<String, Object> param = this.getCentreParamMap(
//                                ((IUrbanBlock) feat), crit, (Town) town);
//                        valeursCourantes.put(crit.getName(),
//                                new Double(crit.value(param)));
//                    }
//                    String res = electre
//                            .decision(criteria, valeursCourantes, conclusion)
//                            .getCategory();
//
//                    int electreClass = 0;
//                    if ("very good".equals(res))
//                        electreClass = 4;
//                    else if ("good".equals(res))
//                        electreClass = 3;
//                    else if ("average".equals(res))
//                        electreClass = 2;
//                    else if ("bad".equals(res))
//                        electreClass = 1;
//
//                    // ******************** compute SVM classification
//                    List<BuildingClass> svmClasses = new ArrayList<>();
//                    for (IUrbanElement building : ((IUrbanBlock) feat)
//                            .getUrbanElements()) {
//                        svmClasses.add(svm.predict(building));
//                    }
//                    int svmClass = 0;
//                    if (svmClasses.size() != 0) {
//
//                        int nbInner = 0, nbUrban = 0, nbSub = 0, nbIndus = 0,
//                                nbRural = 0;
//                        for (BuildingClass svmClassValue : svmClasses) {
//                            if (svmClassValue.equals(BuildingClass.INDUSTRY))
//                                nbIndus++;
//                            else if (svmClassValue
//                                    .equals(BuildingClass.INNER_CITY))
//                                nbInner++;
//                            else if (svmClassValue.equals(BuildingClass.URBAN))
//                                nbUrban++;
//                            else if (svmClassValue
//                                    .equals(BuildingClass.SUBURBAN))
//                                nbSub++;
//                            else
//                                nbRural++;
//                        }
//
//                        // check if one class has the majority
//
//                        if (nbInner > svmClasses.size() / 2)
//                            svmClass = 4;
//                        else if (nbUrban > svmClasses.size() / 2)
//                            svmClass = 3;
//                        else if (nbSub > svmClasses.size() / 2)
//                            svmClass = 2;
//                        else if (nbIndus > svmClasses.size() / 2)
//                            svmClass = 2;
//                        else if (nbRural > svmClasses.size() / 2)
//                            svmClass = 1;
//                        else
//                            svmClass = 1;
//                    }
//
//                    // ******************** compute deep classification
//                    MultiLayerNetwork model;
//                    int deepClass = -1;
//                    try {
//                        model = KerasModelImport
//                                .importKerasSequentialModelAndWeights(new File(
//                                        "D:\\workspace\\gtouya-CartAGen\\src\\main\\resources\\tensorflow\\cnn_blocks.hdf5")
//                                                .getPath());
//                        // create the image from the block
//                        BufferedImage bi = new Block2Image((IUrbanBlock) feat)
//                                .createGrayBufferedImage(128, 5);
//                        INDArray array = Nd4j.zeros(128, 128);
//                        for (int j = 0; j < 128; j++) {
//                            for (int k = 0; k < 128; k++) {
//                                array.put(j, k, bi.getRGB(j, k));
//                            }
//                        }
//                        deepClass = model.predict(array)[0];
//                        System.out.println(deepClass);
//                    } catch (InvalidKerasConfigurationException
//                            | UnsupportedKerasConfigurationException e) {
//                        e.printStackTrace();
//                    }
//
//                    // *************** write a new line in the excel sheet
//                    sheet.addCell(
//                            new Label(0, i, String.valueOf(feat.getId())));
//                    sheet.addCell(new jxl.write.Number(1, i, electreClass));
//                    sheet.addCell(new jxl.write.Number(2, i, svmClass));
//                    sheet.addCell(new jxl.write.Number(3, i, deepClass));
//
//                    i++;
//                }
//
//                workbook.write();
//                workbook.close();
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            } catch (WriteException e1) {
//                e1.printStackTrace();
//            } catch (Exception e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//
//        private ConclusionIntervals initCentreConclusion(
//                Set<Criterion> criteria) {
//            ConclusionIntervals conclusion = new ConclusionIntervals(criteria);
//            Map<String, Double> borneSupTB = new Hashtable<String, Double>();
//            Map<String, Double> borneInfTB = new Hashtable<String, Double>();
//            Map<String, Double> borneInfB = new Hashtable<String, Double>();
//            Map<String, Double> borneInfMy = new Hashtable<String, Double>();
//            Map<String, Double> borneInfMv = new Hashtable<String, Double>();
//            Map<String, Double> borneInfTMv = new Hashtable<String, Double>();
//
//            Iterator<Criterion> itc = criteria.iterator();
//            while (itc.hasNext()) {
//                Criterion ct = itc.next();
//                borneSupTB.put(ct.getName(), new Double(1));
//                borneInfTB.put(ct.getName(), new Double(0.8));
//                borneInfB.put(ct.getName(), new Double(0.6));
//                borneInfMy.put(ct.getName(), new Double(0.4));
//                borneInfMv.put(ct.getName(), new Double(0.2));
//                borneInfTMv.put(ct.getName(), new Double(0));
//            }
//            conclusion.addInterval(borneInfTMv, borneInfMv, "very bad");
//            conclusion.addInterval(borneInfMv, borneInfMy, "bad");
//            conclusion.addInterval(borneInfMy, borneInfB, "average");
//            conclusion.addInterval(borneInfB, borneInfTB, "good");
//            conclusion.addInterval(borneInfTB, borneSupTB, "very good");
//            return conclusion;
//        }
//
//        private Map<String, Object> getCentreParamMap(IUrbanBlock block,
//                Criterion crit, Town town) {
//            Map<String, Object> param = new HashMap<String, Object>();
//            param.put("block", block);
//            param.put("buildingAreaStats", town.getBuildAreaStats());
//            if (crit.getName().equals("BuildingArea")) {
//                param.put("meanBuildingArea", town.getMeanBuildArea());
//            } else if (crit.getName().equals("Centroid")) {
//                IDirectPosition centrePos = town.getCentre();
//                if (centrePos == null)
//                    centrePos = town.getGeom().centroid();
//                param.put("centroid", centrePos);
//                Point pt;
//                try {
//                    pt = CommonAlgorithms.getPointLePlusLoin(
//                            (Point) JtsGeOxygene
//                                    .makeJtsGeom(centrePos.toGM_Point()),
//                            (Polygon) JtsGeOxygene.makeJtsGeom(town.getGeom()));
//                    IDirectPosition dp = JtsGeOxygene
//                            .makeDirectPosition(pt.getCoordinateSequence());
//                    double maxDist = dp.distance2D(centrePos);
//                    param.put("max_dist", maxDist);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            } else if (crit.getName().equals("Area")) {
//                param.put("meanBlockArea", town.getMeanBlockArea());
//            } else if (crit.getName().equals("Limit")) {
//                param.put("outline", town.getOutline());
//            }
//            return param;
//        }
//
//        public CompareBlocksAction() {
//            this.putValue(Action.SHORT_DESCRIPTION,
//                    "Compare classification methods on the selected blocks");
//            this.putValue(Action.NAME, "Compare methods");
//        }
//
//    }

}
