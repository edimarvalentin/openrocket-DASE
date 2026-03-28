package org.dase;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.models.wind.PinkNoiseWindModel;
import info.openrocket.core.models.wind.WindModelType;
import info.openrocket.core.rocketcomponent.*;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.startup.OpenRocketCore;
import info.openrocket.core.util.GeodeticComputationStrategy;


import java.io.File;
import java.util.List;

public class OpenRocket {

    Rocket rocket;
    Simulation sim;

    OpenRocket(File file) {
        // Create the core module
        OpenRocketCore.initialize();
        rocket = loadRocketFromFile(file);
    }

    private Rocket loadRocketFromFile(File file){
        try{
            GeneralRocketLoader loader = new GeneralRocketLoader(file);
            OpenRocketDocument document = loader.load();
            return document.getRocket();
        } catch (Exception e){
            System.err.println("Failed to load rocket from file: " + e.getMessage());
            return null;
        }
    }


    public void setSimulation(){
        sim = new Simulation(rocket);
        SimulationOptions options = sim.getOptions();

        // 805 Parking Lot
        options.setLaunchAltitude(0); // m
        options.setLaunchLatitude(32.10894);
        options.setLaunchLongitude(-110.94395);

        // Launch Rod
        options.setLaunchRodLength(100); // cm
        options.setLaunchRodAngle(5); // deg
        options.setLaunchRodDirection(90); // deg

        options.setISAAtmosphere(true); // Use International Standard Atmosphere

        options.setWindModelType(WindModelType.AVERAGE); // Use the average wind model type

        options.setGeodeticComputation(GeodeticComputationStrategy.SPHERICAL); // Spherical approximation geodetic calculations

        // Wind
        PinkNoiseWindModel windModel = options.getAverageWindModel();

        // speed
        windModel.setAverage(2); // m/s

        // standard deviation
        windModel.setStandardDeviation(0.2); // m/s

        // turbulence intensity
        windModel.setTurbulenceIntensity(10); // %

        // direction
        windModel.setDirection(90); // deg

        // Sim clock
        options.setTimeStep(0.05);
        options.setMaxSimulationTime(1200);
    }


    public void runSimulation(){
        try {
            sim.simulate();
            sim.getSimulatedData();
        } catch (Exception e){
            System.err.println("Failed to run sim: " + e.getMessage());
        }
    }

    public void setRocketTubeLength(double length){
        RocketComponent sustainer = rocket.getChild(0);
        List<RocketComponent> parts = sustainer.getChildren();

        // Find the tube
        for (RocketComponent part : parts) {
            if (part.getComponentName().equals("Body Tube")) {
                BodyTube tube = (BodyTube) part;
                tube.setLength(length);
                return;
            }
        }
    }

    public void setRocketConeLength(double length){
        RocketComponent sustainer = rocket.getChild(0);
        List<RocketComponent> parts = sustainer.getChildren();

        // Find the cone
        for(RocketComponent part : parts){
            if(part.getComponentName().equals("Nose Cone")){
                NoseCone cone = (NoseCone) part;
                cone.setLength(length);
                return;
            }
        }
    }

    public void setRocketFinRelativePlacement(double displacement){
        RocketComponent sustainer = rocket.getChild(0);
        List<RocketComponent> parts = sustainer.getChildren();
        // Find the fins
        for(RocketComponent part : parts){
            if(part.getComponentName().equals("BodyTube")){
                List<RocketComponent> children = part.getChildren();
                for(RocketComponent child : children){
                    if(child.getComponentName().equals("Freeform Fin Set")){
                        FreeformFinSet fins = (FreeformFinSet) child;
                        fins.setAxialMethod(AxialMethod.BOTTOM);
                        fins.setAxialOffset(displacement);
                        return;
                    }
                }
            }
        }
    }

    public void setRocketConeShape(String type){
        RocketComponent sustainer = rocket.getChild(0);
        List<RocketComponent> parts = sustainer.getChildren();

        // Find the cone
        for(RocketComponent part : parts){
            if(part.getComponentName().equals("Nose Cone")){
                NoseCone cone = (NoseCone) part;
                if (type.equals("conical")){
                    cone.setShapeType(Transition.Shape.CONICAL);
                } else if (type.equals("ellipsoid")) {
                    cone.setShapeType(Transition.Shape.ELLIPSOID);
                }
            }
        }
    }

    public void setNumFins(int num){
        RocketComponent sustainer = rocket.getChild(0);
        List<RocketComponent> parts = sustainer.getChildren();
        // Find the fins
        for(RocketComponent part : parts){
            if(part.getComponentName().equals("BodyTube")){
                List<RocketComponent> children = part.getChildren();
                for(RocketComponent child : children){
                    if(child.getComponentName().equals("Freeform Fin Set")){
                        FreeformFinSet fins = (FreeformFinSet) child;
                        fins.setFinCount(num);
                        return;
                    }
                }
            }
        }
    }

    public void setWindVelocity(double velocity)
    {
        SimulationOptions options = sim.getOptions();
        PinkNoiseWindModel windModel = options.getAverageWindModel();
        windModel.setAverage(velocity);
    }

    public void setRodAngle(double angle_rod)
    {
        SimulationOptions options = sim.getOptions();
        options.setLaunchRodAngle(angle_rod);
    }

    public double getApogee(){
        FlightData results = sim.getSimulatedData();
        return results.getMaxAltitude();
    }
}
