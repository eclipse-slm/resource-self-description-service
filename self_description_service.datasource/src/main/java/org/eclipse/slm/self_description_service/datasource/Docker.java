package org.eclipse.slm.self_description_service.datasource;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.apache.poi.hpsf.Decimal;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;
import org.springframework.boot.task.SimpleAsyncTaskSchedulerBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

@Component
public class Docker implements Datasource {

    private final SimpleAsyncTaskSchedulerBuilder simpleAsyncTaskSchedulerBuilder;
    DockerClient dockerClient;
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy KK:mm:ss a");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public Docker(SimpleAsyncTaskSchedulerBuilder simpleAsyncTaskSchedulerBuilder) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://localhost:2375")
                .withDockerTlsVerify(false)
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        dockerClient = DockerClientImpl.getInstance(config, httpClient);
        this.simpleAsyncTaskSchedulerBuilder = simpleAsyncTaskSchedulerBuilder;
    }

    @Override
    public List<Submodel> getModels() {

        var submodelBuilder = new DefaultSubmodel.Builder();

        var builders = List.of(
                createContainersList(),
                createImageList(),
                createNetworksList(),
                createVolumesList(),
                createServicesList(),
                createTasksList(),
                createSwarmNodesList(),
                createConfigsList(),
                createSecretsList()
        );


        builders.forEach(builder -> {
            builder.ifPresent(b -> submodelBuilder.submodelElements(b.build()));
        });

        return List.of();
    }

    @Override
    public List<String> getModelIds() {
        return List.of();
    }

    @Override
    public Optional<Submodel> getModelById(String id) throws IOException {
        return Optional.empty();
    }

    private Optional<DefaultSubmodelElementList.Builder> createContainersList() {
        var containers = dockerClient.listContainersCmd().exec();

        if (containers.isEmpty()) {
            return Optional.empty();
        }

        var containersListBuilder = new DefaultSubmodelElementList.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text("Containers").build());

        for (Container container : containers) {

            var containerCollectionBuilder = new DefaultSubmodelElementCollection.Builder();

            addProperty(containerCollectionBuilder, container.getId(), "ID");

            var names = new DefaultSubmodelElementList.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text("Names").build());
            for (String name : container.getNames()) {
                addProperty(names, name, "Name");
            }
            containerCollectionBuilder.value(names.build());

            addProperty(containerCollectionBuilder, container.getImage(), "Image");
            addProperty(containerCollectionBuilder, container.getImageId(), "ImageId");

            var labels = container.getLabels();
            addLabels(containerCollectionBuilder, labels);

            addProperty(containerCollectionBuilder, container.getState(), "State");
            addProperty(containerCollectionBuilder, container.getStatus(), "Status");
            addDateTimeProperty(containerCollectionBuilder, container.getCreated(), "Created");


            var networkSettings = container.getNetworkSettings();
            if (networkSettings != null && !networkSettings.getNetworks().isEmpty()) {
                var networkSettingList = new DefaultSubmodelElementList.Builder()
                        .displayName(new DefaultLangStringNameType.Builder().text("Network Settings").build());

                var networks = networkSettings.getNetworks();

                networks.forEach((name, containerNetwork) -> {
                    var network = new DefaultSubmodelElementCollection.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text(name).build());

                    addProperty(network, containerNetwork.getNetworkID(), "Network ID");
                    addProperty(network, containerNetwork.getEndpointId(), "Endpoint ID");
                    addProperty(network, containerNetwork.getMacAddress(), "Mac Address");
                    addProperty(network, containerNetwork.getGateway(), "Gateway");
                    addProperty(network, containerNetwork.getIpAddress(), "Ip Address");


                    var containerLinks = containerNetwork.getLinks();
                    if (containerLinks != null && containerLinks.length > 0) {
                        var linksList = new DefaultSubmodelElementList.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("Links").build());

                        for (Link containerLink : containerLinks) {
                            var link = new DefaultSubmodelElementCollection.Builder();
                            addProperty(link, containerLink.getName(), "Name");
                            addProperty(link, containerLink.getAlias(), "Alias");

                            linksList.value(link.build());
                        }
                        network.value(linksList.build());
                    }

                    var containerAliases = containerNetwork.getAliases();
                    addListOfStringProperties(network, containerAliases, "Aliases", "Alias");

                    networkSettingList.value(network.build());
                });


                containerCollectionBuilder.value(networkSettingList.build());
            }

            containersListBuilder.value(containerCollectionBuilder.build());
        }
        return Optional.of(containersListBuilder);
    }


    private Optional<DefaultSubmodelElementList.Builder> createImageList() {
        var images = dockerClient.listImagesCmd().exec();

        if (images.isEmpty()) {
            return Optional.empty();
        }

        var imageListBuilder = new DefaultSubmodelElementList.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text("Images").build());

        for (Image image : images) {
            var imageBuilder = new DefaultSubmodelElementCollection.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text("Image").build());

            addProperty(imageBuilder, image.getId(), "ID");
            addProperty(imageBuilder, image.getParentId(), "Parent ID");
            addDateTimeProperty(imageBuilder, image.getCreated(), "Created");
            addProperty(imageBuilder, image.getContainers(), "Containers");


            var imageLabels = image.getLabels();
            addLabels(imageBuilder, imageLabels);

            var imageRepoTags = image.getRepoTags();
            addListOfStringProperties(imageBuilder, imageRepoTags, "Repo Tags", "Repo Tag");

            var imageRepoDigests = image.getRepoDigests();
            addListOfStringProperties(imageBuilder, imageRepoDigests, "Repo Digests", "Repo Digest");

            imageListBuilder.value(imageBuilder.build());
        }
        return Optional.of(imageListBuilder);
    }


    private Optional<DefaultSubmodelElementList.Builder> createNetworksList() {
        var networks = dockerClient.listNetworksCmd().exec();

        if (networks.isEmpty()) {
            return Optional.empty();
        }

        var networksListBuilder = new DefaultSubmodelElementList.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text("Networks").build());

        for (Network network : networks) {
            var networkBuilder = new DefaultSubmodelElementCollection.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text("Network").build());

            addProperty(networkBuilder, network.getId(), "ID");
            addProperty(networkBuilder, network.getName(), "Name");
            addProperty(networkBuilder, network.getDriver(), "Driver");
            addProperty(networkBuilder, network.getScope(), "Scope");
            addProperty(networkBuilder, network.getEnableIPv6(), "Enable IPv6");
            addProperty(networkBuilder, network.getInternal(), "Internal");
            addDateTimeProperty(networkBuilder, network.getCreated(), "Created");


            var containers = network.getContainers();
            if (!containers.isEmpty()) {
                var containersListBuilder = new DefaultSubmodelElementList.Builder()
                        .displayName(new DefaultLangStringNameType.Builder().text("Containers").build());

                containers.forEach((key, value) -> {
                    var containerNetworkConfig = new DefaultSubmodelElementCollection.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text(key).build());

                    addProperty(containerNetworkConfig, value.getName(), "Name");
                    addProperty(containerNetworkConfig, value.getEndpointId(), "Endpoint ID");
                    addProperty(containerNetworkConfig, value.getIpv4Address(), "IPv4 Address");
                    addProperty(containerNetworkConfig, value.getIpv6Address(), "IPv6 Address");
                    addProperty(containerNetworkConfig, value.getMacAddress(), "Mac Address");

                    containersListBuilder.value(containerNetworkConfig.build());
                });

                networkBuilder.value(containersListBuilder.build());
            }

            var labels = network.getLabels();
            addLabels(networkBuilder, labels);

            var options = network.getOptions();
            addOptions(networkBuilder, options);

            var ipam = network.getIpam();

            if (ipam != null) {
                var ipamBuilder = new DefaultSubmodelElementCollection.Builder()
                        .displayName(new DefaultLangStringNameType.Builder().text("IPAM").build());

                addProperty(ipamBuilder, ipam.getDriver(), "Driver");

                var ipamOptions = ipam.getOptions();
                addOptions(ipamBuilder, ipamOptions);

                var ipamConfigs = ipam.getConfig();

                if (ipamConfigs != null && !ipamConfigs.isEmpty()) {
                    var ipamConfigsBuilder = new DefaultSubmodelElementList.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("Config").build());

                    ipamConfigs.forEach(config -> {

                        var configBuilder = new DefaultSubmodelElementCollection.Builder();
                        addProperty(configBuilder, config.getNetworkID(), "Network ID");
                        addProperty(configBuilder, config.getGateway(), "Gateway");
                        addProperty(configBuilder, config.getIpRange(), "IP Range");
                        addProperty(configBuilder, config.getSubnet(), "Subnet");

                        ipamConfigsBuilder.value(configBuilder.build());
                    });

                    ipamBuilder.value(ipamConfigsBuilder.build());
                }

                networkBuilder.value(ipamBuilder.build());
            }

            networksListBuilder.value(networkBuilder.build());
        }

        return Optional.of(networksListBuilder);
    }


    private Optional<DefaultSubmodelElementList.Builder> createVolumesList() {
        var volumes = dockerClient.listVolumesCmd().exec();

        if (volumes.getVolumes().isEmpty()) {
            return Optional.empty();
        }

        var volumeListBuilder = new DefaultSubmodelElementList.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text("Volumes").build());

        for (InspectVolumeResponse volume : volumes.getVolumes()) {
            var volumeBuilder = new DefaultSubmodelElementCollection.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text("Volume").build());


            addProperty(volumeBuilder, volume.getName(), "Name");
            addProperty(volumeBuilder, volume.getDriver(), "Driver");
            addProperty(volumeBuilder, volume.getMountpoint(), "Mount point");


            var volumeOptions = volume.getOptions();
            addOptions(volumeBuilder, volumeOptions);

            var volumeLabels = volume.getLabels();
            addLabels(volumeBuilder, volumeLabels);

            volumeListBuilder.value(volumeBuilder.build());
        }


        return Optional.of(volumeListBuilder);
    }

    private Optional<DefaultSubmodelElementList.Builder> createServicesList() {
        var services = dockerClient.listServicesCmd().exec();

        if (services.isEmpty()) {
            return Optional.empty();
        }

        var serviceListBuilder = new DefaultSubmodelElementList.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text("Services").build());

        for (Service service : services) {
            var serviceBuilder = new DefaultSubmodelElementCollection.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text("Service").build());

            addProperty(serviceBuilder, service.getId(), "ID");

            if (service.getVersion() != null && service.getVersion().getIndex() != null) {
                addProperty(serviceBuilder, service.getVersion().getIndex().toString(), "Version");
            }

            addDateTimeProperty(serviceBuilder, service.getCreatedAt(), "Created At");
            addDateTimeProperty(serviceBuilder, service.getUpdatedAt(), "Updated At");


            var endpoint = service.getEndpoint();
            if (endpoint != null) {
                var endpointBuilder = new DefaultSubmodelElementCollection.Builder()
                        .displayName(new DefaultLangStringNameType.Builder().text("Endpoint").build());

                var spec = endpoint.getSpec();
                if (spec != null) {
                    var specBuilder = new DefaultSubmodelElementCollection.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("Spec").build());

                    if (spec.getMode() != null) {
                        addProperty(specBuilder, spec.getMode().toString(), "Mode");
                    }

                    var ports = spec.getPorts();
                    if (ports != null && !ports.isEmpty()) {
                        var portsBuilder = new DefaultSubmodelElementList.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("Ports").build());

                        for (PortConfig port : ports) {
                            var portBuilder = new DefaultSubmodelElementCollection.Builder()
                                    .displayName(new DefaultLangStringNameType.Builder().text("Port").build());
                            addProperty(portBuilder, port.getName(), "Name");
                            addProperty(portBuilder, port.getPublishedPort(), "Published Port");
                            addProperty(portBuilder, port.getTargetPort(), "Target Port");


                            if (port.getPublishMode() != null) {
                                addProperty(portBuilder, port.getPublishMode().toString(), "Publish Mode");
                            }

                            if (port.getProtocol() != null) {
                                addProperty(portBuilder, port.getProtocol().toString(), "Protocol");
                            }

                            portsBuilder.value(portBuilder.build());
                        }

                        specBuilder.value(portsBuilder.build());
                    }

                    endpointBuilder.value(specBuilder.build());
                }

                var ports = endpoint.getPorts();
                if (ports != null && ports.length > 0) {
                    var portsBuilder = new DefaultSubmodelElementList.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("Ports").build());

                    for (PortConfig port : ports) {
                        var portBuilder = new DefaultSubmodelElementCollection.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("Port").build());
                        addProperty(portBuilder, port.getName(), "Name");
                        addProperty(portBuilder, port.getPublishedPort(), "Published Port");
                        addProperty(portBuilder, port.getTargetPort(), "Target Port");


                        if (port.getPublishMode() != null) {
                            addProperty(portBuilder, port.getPublishMode().toString(), "Publish Mode");
                        }

                        if (port.getProtocol() != null) {
                            addProperty(portBuilder, port.getProtocol().toString(), "Protocol");
                        }

                        portsBuilder.value(portBuilder.build());
                    }

                    endpointBuilder.value(portsBuilder.build());
                }

                var virtualIPs = endpoint.getVirtualIPs();
                if (virtualIPs != null && virtualIPs.length > 0) {
                    var virtualIPsBuilder = new DefaultSubmodelElementList.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("VirtualIPs").build());
                    for (EndpointVirtualIP virtualIP : virtualIPs) {
                        var virtualIPBuilder = new DefaultSubmodelElementCollection.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("VirtualIP").build());
                        addProperty(virtualIPBuilder, virtualIP.getAddr(), "Address");
                        addProperty(virtualIPBuilder, virtualIP.getNetworkID(), "Network ID");

                        virtualIPsBuilder.value(virtualIPBuilder.build());
                    }

                    endpointBuilder.value(virtualIPsBuilder.build());
                }

                serviceBuilder.value(endpointBuilder.build());
            }

            serviceListBuilder.value(serviceBuilder.build());
        }

        return Optional.of(serviceListBuilder);
    }

    private Optional<DefaultSubmodelElementList.Builder> createTasksList() {

        var tasks = dockerClient.listTasksCmd().exec();

        if (tasks == null) {
            return Optional.empty();
        }

        if (tasks.isEmpty()) {
            return Optional.empty();
        }

        var tasksListBuilder = new DefaultSubmodelElementList.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text("Tasks").build());

        for (Task task : tasks) {
            var taskBuilder = new DefaultSubmodelElementCollection.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text("Task").build());

            addProperty(taskBuilder, task.getId(), "ID");
            addProperty(taskBuilder, task.getName(), "Name");
            addProperty(taskBuilder, task.getCreatedAt(), "Created At");
            addProperty(taskBuilder, task.getUpdatedAt(), "Updated At");
            addProperty(taskBuilder, task.getNodeId(), "Node ID");
            addProperty(taskBuilder, task.getServiceId(), "Service ID");
            addProperty(taskBuilder, task.getSlot(), "Slot");
            addLabels(taskBuilder, task.getLabels());
            addProperty(taskBuilder, task.getDesiredState().getValue(), "Desired State");
            addProperty(taskBuilder, task.getVersion().getIndex(), "Version Index");

            var spec = task.getSpec();
            if (spec != null) {
                var specBuilder = new DefaultSubmodelElementCollection.Builder()
                        .displayName(new DefaultLangStringNameType.Builder().text("Spec").build());

                addProperty(specBuilder, spec.getForceUpdate(), "Force Update");
                addProperty(specBuilder, spec.getRuntime(), "Runtime");

                var containerSpec = spec.getContainerSpec();
                if (containerSpec != null) {
                    var containerSpecBuilder = new DefaultSubmodelElementCollection.Builder();
                    // TODO Implement


                    specBuilder.value(containerSpecBuilder.build());
                }

                var networks = spec.getNetworks();
                if (networks != null && !networks.isEmpty()) {
                    var networksBuilder = new DefaultSubmodelElementList.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("Networks").build());

                    for (NetworkAttachmentConfig network : networks) {
                        var networkBuilder = new DefaultSubmodelElementCollection.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("Network").build());

                        addProperty(networkBuilder, network.getTarget(), "Target");
                        addListOfStringProperties(networkBuilder, network.getAliases(), "Aliases", "Alias");
                        networksBuilder.value(networkBuilder.build());
                    }

                    specBuilder.value(networksBuilder.build());
                }

                var servicePlacement = spec.getPlacement();
                if (servicePlacement != null) {
                    var servicePlacementBuilder = new DefaultSubmodelElementCollection.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("Service Placement").build());
                    addListOfStringProperties(servicePlacementBuilder, servicePlacement.getConstraints(), "Constraints", "Constraint");
                    addProperty(servicePlacementBuilder, servicePlacement.getMaxReplicas(), "Max Replicas");

                    var platforms = servicePlacement.getPlatforms();
                    if (platforms != null && !platforms.isEmpty()) {
                        var platformsBuilder = new DefaultSubmodelElementList.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("Platforms").build());
                        for (var platform : platforms) {
                            var platformBuilder = new DefaultSubmodelElementCollection.Builder()
                                    .displayName(new DefaultLangStringNameType.Builder().text("Platform").build());
                            addProperty(platformBuilder, platform.getArchitecture(), "Architecture");
                            addProperty(platformBuilder, platform.getOs(), "OS");
                            platformsBuilder.value(platformBuilder.build());
                        }

                        servicePlacementBuilder.value(platformsBuilder.build());
                    }

                    specBuilder.value(servicePlacementBuilder.build());
                }

                var resources = spec.getResources();
                if (resources != null) {
                    var resourcesBuilder = new DefaultSubmodelElementCollection.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("Resources").build());

                    var limits = resources.getLimits();
                    if (limits != null) {
                        var limitsBuilder = new DefaultSubmodelElementCollection.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("Limits").build());

                        addProperty(limitsBuilder, limits.getMemoryBytes(), "Memory Bytes");
                        addProperty(limitsBuilder, limits.getNanoCPUs(), "Nano CPUs");

                        resourcesBuilder.value(limitsBuilder.build());
                    }

                    var reservations = resources.getReservations();
                    if (reservations != null) {
                        var reservationsBuilder = new DefaultSubmodelElementCollection.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("Reservations").build());

                        addProperty(reservationsBuilder, reservations.getMemoryBytes(), "Memory Bytes");
                        addProperty(reservationsBuilder, reservations.getNanoCPUs(), "Nano CPUs");

                        resourcesBuilder.value(reservationsBuilder.build());
                    }

                    specBuilder.value(resourcesBuilder.build());
                }

                var logDriver = spec.getLogDriver();
                if (logDriver != null) {
                    var logDriverBuilder = new DefaultSubmodelElementCollection.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("Log Driver").build());

                    addProperty(logDriverBuilder, logDriver.getName(), "Name");
                    addOptions(logDriverBuilder, logDriver.getOptions());

                    specBuilder.value(logDriverBuilder.build());
                }

                var restartPolicy = spec.getRestartPolicy();
                if (restartPolicy != null) {
                    var restartPolicyBuilder = new DefaultSubmodelElementCollection.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("RestartPolicy").build());

                    addProperty(restartPolicyBuilder, restartPolicy.getDelay(), "Delay");
                    addProperty(restartPolicyBuilder, restartPolicy.getWindow(), "Window");
                    addProperty(restartPolicyBuilder, restartPolicy.getMaxAttempts(), "Max Attempts");

                    var condition = restartPolicy.getCondition();
                    if (condition != null) {
                        addProperty(restartPolicyBuilder, condition.name(), "Condition");
                    }

                    specBuilder.value(restartPolicyBuilder.build());
                }

                taskBuilder.value(specBuilder.build());
            }

            var status = task.getStatus();
            if (status != null) {
                var statusBuilder = new DefaultSubmodelElementCollection.Builder()
                        .displayName(new DefaultLangStringNameType.Builder().text("Status").build());

                addProperty(statusBuilder, status.getErr(), "Error");
                addProperty(statusBuilder, status.getMessage(), "Message");
                addProperty(statusBuilder, status.getTimestamp(), "Timestamp");
                addProperty(statusBuilder, status.getState().getValue(), "State");

                var containerStatus = status.getContainerStatus();
                if (containerStatus != null) {
                    var containerStatusBuilder = new DefaultSubmodelElementCollection.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("Container Status").build());

                    addProperty(containerStatusBuilder, containerStatus.getContainerID(), "Container ID");
                    addProperty(containerStatusBuilder, containerStatus.getExitCodeLong(), "Exit Code Long");
                    addProperty(containerStatusBuilder, containerStatus.getPidLong(), "Pid Long");

                    statusBuilder.value(containerStatusBuilder.build());
                }

                taskBuilder.value(statusBuilder.build());
            }

            tasksListBuilder.value(taskBuilder.build());
        }

        return Optional.of(tasksListBuilder);
    }

    private Optional<DefaultSubmodelElementList.Builder> createSwarmNodesList() {
        var swarmNodes = dockerClient.listSwarmNodesCmd().exec();
        if (swarmNodes == null || swarmNodes.isEmpty()) {
            return Optional.empty();
        }

        var swarmBuilder = new DefaultSubmodelElementList.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text("Swarm Nodes").build());
        for (SwarmNode swarmNode : swarmNodes) {
            var swarmNodeBuilder = new DefaultSubmodelElementCollection.Builder();

            addProperty(swarmNodeBuilder, swarmNode.getId(), "ID");
            addDateTimeProperty(swarmNodeBuilder, swarmNode.getCreatedAt(), "Created At");
            addDateTimeProperty(swarmNodeBuilder, swarmNode.getUpdatedAt(), "Updated At");

            if (swarmNode.getVersion() != null) {
                addProperty(swarmNodeBuilder, swarmNode.getVersion().getIndex(), "Version");
            }

            var description = swarmNode.getDescription();
            if (description != null) {
                var descriptionBuilder = new DefaultSubmodelElementCollection.Builder()
                        .displayName(new DefaultLangStringNameType.Builder().text("Description").build());

                addProperty(descriptionBuilder, description.getHostname(), "Hostname");

                var engine = description.getEngine();
                if (engine != null) {
                    var engineBuilder = new DefaultSubmodelElementCollection.Builder();

                    addProperty(engineBuilder, engine.getEngineVersion(), "Engine Version");
                    addLabels(engineBuilder, engine.getLabels());

                    var plugins = engine.getPlugins();
                    if (plugins != null) {
                        var pluginsBuilder = new DefaultSubmodelElementList.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("Plugins").build());

                        for (SwarmNodePluginDescription plugin : plugins) {
                            var pluginBuilder = new DefaultSubmodelElementCollection.Builder()
                                    .displayName(new DefaultLangStringNameType.Builder().text("Plugin").build());
                            addProperty(pluginBuilder, plugin.getName(), "Name");
                            addProperty(pluginBuilder, plugin.getType(), "Type");

                            pluginsBuilder.value(pluginBuilder.build());
                        }

                        engineBuilder.value(pluginsBuilder.build());
                    }


                    descriptionBuilder.value(engineBuilder.build());
                }

                var resources = description.getResources();
                if (resources != null) {
                    var resourcesBuilder = new DefaultSubmodelElementCollection.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("Resources").build());
                    addProperty(resourcesBuilder, resources.getMemoryBytes(), "Memory Bytes");
                    addProperty(resourcesBuilder, resources.getNanoCPUs(), "Nano CPUs");

                    descriptionBuilder.value(resourcesBuilder.build());
                }

                var platform = description.getPlatform();
                if (platform != null) {
                    var platformBuilder = new DefaultSubmodelElementCollection.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("Platform").build());
                    addProperty(platformBuilder, platform.getArchitecture(), "Architecture");
                    addProperty(platformBuilder, platform.getOs(), "Os");

                    descriptionBuilder.value(platformBuilder.build());
                }

                swarmNodeBuilder.value(descriptionBuilder.build());
            }

            var managerStatus = swarmNode.getManagerStatus();
            if (managerStatus != null) {
                var managerStatusBuilder = new DefaultSubmodelElementCollection.Builder()
                        .displayName(new DefaultLangStringNameType.Builder().text("Manager Status").build());
                addProperty(managerStatusBuilder, managerStatus.getAddr(), "Address");
                if (managerStatus.getReachability() != null) {
                    addProperty(managerStatusBuilder, managerStatus.getReachability().name(), "Reachability");
                }

                swarmNodeBuilder.value(managerStatusBuilder.build());
            }

            var status = swarmNode.getStatus();
            if (status != null) {
                var statusBuilder = new DefaultSubmodelElementCollection.Builder()
                        .displayName(new DefaultLangStringNameType.Builder().text("Status").build());

                addProperty(statusBuilder, status.getAddress(), "Address");
                if (status.getState() != null) {
                    addProperty(statusBuilder, status.getState().name(), "State");
                }

                swarmNodeBuilder.value(statusBuilder.build());
            }

            var spec = swarmNode.getSpec();
            if (spec != null) {
                var specBuilder = new DefaultSubmodelElementCollection.Builder()
                        .displayName(new DefaultLangStringNameType.Builder().text("Spec").build());
                addProperty(specBuilder, spec.getName(), "Name");
                addLabels(specBuilder, spec.getLabels());

                if (spec.getAvailability() != null) {
                    addProperty(specBuilder, spec.getAvailability().name(), "Availability");
                }

                if (spec.getRole() != null) {
                    addProperty(specBuilder, spec.getRole().name(), "Role");
                }

                swarmNodeBuilder.value(specBuilder.build());
            }

            swarmBuilder.value(swarmNodeBuilder.build());
        }


        return Optional.of(swarmBuilder);
    }

    private Optional<DefaultSubmodelElementList.Builder> createConfigsList() {
        var configs = dockerClient.listConfigsCmd().exec();

        if (configs == null || configs.isEmpty()) {
            return Optional.empty();
        }

        var configsListBuilder = new DefaultSubmodelElementList.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text("Configs").build());

        for (Config config : configs) {
            var configBuilder = new DefaultSubmodelElementCollection.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text("Config").build());

            addProperty(configBuilder, config.getId(), "ID");
            addDateTimeProperty(configBuilder, config.getCreatedAt(), "Created At");
            addDateTimeProperty(configBuilder, config.getUpdatedAt(), "Updated At");
            if (config.getVersion() != null) {
                addProperty(configBuilder, config.getVersion().getIndex(), "Version");
            }

            var spec = config.getSpec();
            if (spec != null) {
                var specBuilder = new DefaultSubmodelElementCollection.Builder()
                        .displayName(new DefaultLangStringNameType.Builder().text("Spec").build());
                addProperty(specBuilder, spec.getName(), "Name");
                configBuilder.value(specBuilder.build());
            }

            configsListBuilder.value(configBuilder.build());
        }

        return Optional.of(configsListBuilder);
    }

    private Optional<DefaultSubmodelElementList.Builder> createSecretsList() {
        var secrets = dockerClient.listSecretsCmd().exec();
        if (secrets == null || secrets.isEmpty()) {
            return Optional.empty();
        }

        var secretsListBuilder = new DefaultSubmodelElementList.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text("Secrets").build());

        for (Secret secret : secrets) {
            var secBuilder = new DefaultSubmodelElementCollection.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text("Secret").build());

            addProperty(secBuilder, secret.getId(), "ID");
            addDateTimeProperty(secBuilder, secret.getCreatedAt(), "Created At");
            addDateTimeProperty(secBuilder, secret.getUpdatedAt(), "Updated At");
            if (secret.getVersion() != null) {
                addProperty(secBuilder, secret.getVersion().getIndex(), "Version");
            }

            var spec = secret.getSpec();
            if (spec != null) {
                var specBuilder = new DefaultSubmodelElementCollection.Builder()
                        .displayName(new DefaultLangStringNameType.Builder().text("Spec").build());
                addProperty(specBuilder, spec.getName(), "Name");
                addLabels(specBuilder, spec.getLabels());

                var mode = spec.getMode();
                if (mode != null) {
                    var modeBuilder = new DefaultSubmodelElementCollection.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("Mode").build());

                    if (mode.getMode() != null) {
                        addProperty(modeBuilder, mode.getMode().name(), "Mode");
                    }

                    if (mode.getReplicated() != null) {
                        addProperty(modeBuilder, mode.getReplicated().getReplicas(), "Replicated");
                    }


                    specBuilder.value(modeBuilder.build());
                }

                var endpointSpec = spec.getEndpointSpec();
                if (endpointSpec != null) {
                    var endpointSpecBuilder = new DefaultSubmodelElementCollection.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("Endpoint").build());
                    if (endpointSpec.getMode() != null) {
                        addProperty(endpointSpecBuilder, endpointSpec.getMode().name(), "Mode");
                    }

                    var ports = endpointSpec.getPorts();
                    if (ports != null && !ports.isEmpty()) {
                        var portsListBuilder = new DefaultSubmodelElementList.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("Ports").build());

                        for (PortConfig port : ports) {
                            var portBuilder = new DefaultSubmodelElementCollection.Builder()
                                    .displayName(new DefaultLangStringNameType.Builder().text("Port").build());
                            addProperty(portBuilder, port.getName(), "Name");
                            addProperty(portBuilder, port.getTargetPort(), "Target Port");
                            addProperty(portBuilder, port.getPublishedPort(), "Published Port");

                            if (port.getProtocol() != null) {
                                addProperty(portBuilder, port.getProtocol().name(), "Protocol");
                            }

                            if (port.getPublishMode() != null) {
                                addProperty(portBuilder, port.getPublishMode().name(), "Publish Mode");
                            }

                            portsListBuilder.value(portBuilder.build());
                        }

                        endpointSpecBuilder.value(portsListBuilder.build());
                    }

                    specBuilder.value(endpointSpecBuilder.build());
                }

                var networks = spec.getNetworks();
                if (networks != null && !networks.isEmpty()) {
                    var networksListBuilder = new DefaultSubmodelElementList.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("Networks").build());

                    for (NetworkAttachmentConfig network : networks) {
                        var networkBuilder = new DefaultSubmodelElementCollection.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("Network").build());

                        addListOfStringProperties(networkBuilder, network.getAliases(), "Aliases", "Alias");
                        addProperty(networkBuilder, network.getTarget(), "Target");

                        networksListBuilder.value(networkBuilder.build());
                    }

                    specBuilder.value(networksListBuilder.build());
                }

                var rollbackConfig = spec.getRollbackConfig();
                if (rollbackConfig != null) {
                    var rollbackBuilder = new DefaultSubmodelElementCollection.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("Rollback").build());

                    addProperty(rollbackBuilder, rollbackConfig.getDelay(), "Delay");
                    addProperty(rollbackBuilder, rollbackConfig.getMonitor(), "Monitor");
                    addProperty(rollbackBuilder, rollbackConfig.getParallelism(), "Parallelism");
                    addProperty(rollbackBuilder, rollbackConfig.getMaxFailureRatio(), "Max Failure Ratio");


                    if (rollbackConfig.getFailureAction() != null) {
                        addProperty(rollbackBuilder, rollbackConfig.getFailureAction().name(), "Failure Action");
                    }

                    if (rollbackConfig.getOrder() != null) {
                        addProperty(rollbackBuilder, rollbackConfig.getOrder().name(), "Order");
                    }

                    specBuilder.value(rollbackBuilder.build());
                }

                var taskTemplate = spec.getTaskTemplate();
                if (taskTemplate != null) {
                    var taskBuilder = new DefaultSubmodelElementCollection.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("Task").build());
                    addProperty(taskBuilder, taskTemplate.getRuntime(), "Runtime");
                    addProperty(taskBuilder, taskTemplate.getForceUpdate(), "Force Update");

                    var restartPolicy = taskTemplate.getRestartPolicy();
                    if (restartPolicy != null) {
                        var restartPolicyBuilder = new DefaultSubmodelElementCollection.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("Restart").build());

                        addProperty(restartPolicyBuilder, restartPolicy.getMaxAttempts(), "Max Attempts");
                        addProperty(restartPolicyBuilder, restartPolicy.getWindow(), "Window");
                        addProperty(restartPolicyBuilder, restartPolicy.getDelay(), "Delay");

                        if (restartPolicy.getCondition() != null) {
                            addProperty(restartPolicyBuilder, restartPolicy.getCondition().name(), "Condition");
                        }

                        taskBuilder.value(restartPolicyBuilder.build());
                    }

                    var logDriver = taskTemplate.getLogDriver();
                    if (logDriver != null) {
                        var logBuilder = new DefaultSubmodelElementCollection.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("Log").build());
                        addProperty(logBuilder, logDriver.getName(), "Name");
                        addOptions(logBuilder, logDriver.getOptions());

                        taskBuilder.value(logBuilder.build());
                    }

                    var containerSpec = taskTemplate.getContainerSpec();
                    if (containerSpec != null) {
                        var containerBuilder = new DefaultSubmodelElementCollection.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("Container").build());

                        addListOfStringProperties(containerBuilder, containerSpec.getArgs(), "Args", "Arg");
                        addListOfStringProperties(containerBuilder, containerSpec.getCommand(), "Commands", "Command");

                        var containerSpecSecrets = containerSpec.getSecrets();
                        if (containerSpecSecrets != null && !containerSpecSecrets.isEmpty()) {
                            var containerSpecSecretListBuilder = new DefaultSubmodelElementList.Builder()
                                    .displayName(new DefaultLangStringNameType.Builder().text("Secrets").build());

                            for (ContainerSpecSecret containerSpecSecret : containerSpecSecrets) {
                                var containerSpecSecretBuilder = new DefaultSubmodelElementCollection.Builder()
                                        .displayName(new DefaultLangStringNameType.Builder().text("Secret").build());
                                addProperty(containerSpecSecretBuilder, containerSpecSecret.getSecretId(), "Secret ID");
                                addProperty(containerSpecSecretBuilder, containerSpecSecret.getSecretName(), "Secret");

                                containerSpecSecretListBuilder.value(containerSpecSecretBuilder.build());
                            }

                            containerBuilder.value(containerSpecSecretListBuilder.build());
                        }

                        addLabels(containerBuilder, containerSpec.getLabels());
                        addProperty(containerBuilder, containerSpec.getDir(), "Dir");

                        var containerConfigs = containerSpec.getConfigs();
                        if (containerConfigs != null && !containerConfigs.isEmpty()) {
                            var containerConfigListBuilder = new DefaultSubmodelElementList.Builder()
                                    .displayName(new DefaultLangStringNameType.Builder().text("Configs").build());

                            for (ContainerSpecConfig containerConfig : containerConfigs) {
                                var containerConfigBuilder = new DefaultSubmodelElementCollection.Builder()
                                        .displayName(new DefaultLangStringNameType.Builder().text("Config").build());
                                addProperty(containerConfigBuilder, containerConfig.getConfigID(), "Config ID");
                                addProperty(containerConfigBuilder, containerConfig.getConfigName(), "Config Name");

                                var configFile = containerConfig.getFile();
                                if (configFile != null) {
                                    var configFileBuilder = new DefaultSubmodelElementCollection.Builder();
                                    addProperty(configFileBuilder, configFile.getGid(), "G ID");
                                    addProperty(configFileBuilder, configFile.getName(), "Config File");
                                    addProperty(configFileBuilder, configFile.getMode(), "Mode");
                                    addProperty(configFileBuilder, configFile.getUid(), "UID");

                                    containerConfigBuilder.value(configFileBuilder.build());
                                }

                                containerConfigListBuilder.value(containerConfigBuilder.build());
                            }

                            containerBuilder.value(containerConfigListBuilder.build());
                        }

                        var dnsConfig = containerSpec.getDnsConfig();
                        if (dnsConfig != null) {
                            var dnsConfigBuilder = new DefaultSubmodelElementCollection.Builder()
                                    .displayName(new DefaultLangStringNameType.Builder().text("DNS Config").build());

                            addListOfStringProperties(dnsConfigBuilder, dnsConfig.getOptions(), "Options", "Option");
                            addListOfStringProperties(dnsConfigBuilder, dnsConfig.getNameservers(), "Names Servers", "Name Server");
                            addListOfStringProperties(dnsConfigBuilder, dnsConfig.getSearch(), "Searches", "Search");

                            containerBuilder.value(dnsConfigBuilder.build());
                        }

                        addProperty(containerBuilder, containerSpec.getDuration(), "Duration");
                        addListOfStringProperties(containerBuilder, containerSpec.getEnv(), "Environments", "Environment");
                        addProperty(containerBuilder, containerSpec.getGroups(), "Groups");

                        var healthCheck = containerSpec.getHealthCheck();
                        if (healthCheck != null) {
                            var healthCheckBuilder = new DefaultSubmodelElementCollection.Builder()
                                    .displayName(new DefaultLangStringNameType.Builder().text("Health Check").build());

                            addProperty(healthCheckBuilder, healthCheck.getInterval(), "Interval");
                            addProperty(healthCheckBuilder, healthCheck.getRetries(), "Retries");
                            addProperty(healthCheckBuilder, healthCheck.getStartInterval(), "Start Interval");
                            addProperty(healthCheckBuilder, healthCheck.getTimeout(), "Timeout");
                            addProperty(healthCheckBuilder, healthCheck.getStartPeriod(), "Start Period");
                            addListOfStringProperties(healthCheckBuilder, healthCheck.getTest(), "Tests", "Test");

                            containerBuilder.value(healthCheckBuilder.build());
                        }

                        addListOfStringProperties(containerBuilder, containerSpec.getHosts(), "Hosts", "Host");
                        addProperty(containerBuilder, containerSpec.getImage(), "Image");
                        addProperty(containerBuilder, containerSpec.getHostname(), "Hostname");

                        addProperty(containerBuilder, containerSpec.getInit(), "Init");

                        var mounts = containerSpec.getMounts();
                        if (mounts != null && !mounts.isEmpty()) {
                            var mountListBuilder = new DefaultSubmodelElementList.Builder()
                                    .displayName(new DefaultLangStringNameType.Builder().text("Mounts").build());

                            for (Mount mount : mounts) {
                                var mountBuilder = new DefaultSubmodelElementCollection.Builder()
                                        .displayName(new DefaultLangStringNameType.Builder().text("Mount").build());

                                addProperty(mountBuilder, mount.getReadOnly(), "ReadOnly");
                                addProperty(mountBuilder, mount.getTarget(), "Target");
                                addProperty(mountBuilder, mount.getSource(), "Source");

                                var mountType = mount.getType();
                                if (mountType != null) {
                                    addProperty(mountBuilder, mountType.name(), "Mount Type");
                                }

                                var bindOptions = mount.getBindOptions();
                                if (bindOptions != null) {
                                    var bindOptionBuilder = new DefaultSubmodelElementCollection.Builder()
                                            .displayName(new DefaultLangStringNameType.Builder().text("Bind Options").build());

                                    var propagation = bindOptions.getPropagation();
                                    if (propagation != null) {
                                        addProperty(bindOptionBuilder, propagation.name(), "Propagation");
                                    }

                                    mountBuilder.value(bindOptionBuilder.build());
                                }

                                var tmpfOptions = mount.getTmpfsOptions();
                                if (tmpfOptions != null) {
                                    var tmpfOptionBuilder = new DefaultSubmodelElementCollection.Builder()
                                            .displayName(new DefaultLangStringNameType.Builder().text("Tmpf Options").build());
                                    addProperty(tmpfOptionBuilder, tmpfOptions.getMode(), "Mode");
                                    addProperty(tmpfOptionBuilder, tmpfOptions.getSizeBytes(), "Size Bytes");

                                    mountBuilder.value(tmpfOptionBuilder.build());
                                }

                                var volumeOptions = mount.getVolumeOptions();
                                if (volumeOptions != null) {
                                    var volumeOptionBuilder = new DefaultSubmodelElementCollection.Builder()
                                            .displayName(new DefaultLangStringNameType.Builder().text("Volume Options").build());
                                    addLabels(volumeOptionBuilder, volumeOptions.getLabels());
                                    addProperty(volumeOptionBuilder, volumeOptions.getNoCopy(), "NoCopy");
                                    var driverConfig = volumeOptions.getDriverConfig();
                                    if (driverConfig != null) {
                                        var driverConfigBuilder = new DefaultSubmodelElementCollection.Builder()
                                                .displayName(new DefaultLangStringNameType.Builder().text("Driver Config").build());
                                        addProperty(driverConfigBuilder, driverConfig.getName(), "Name");
                                        addOptions(driverConfigBuilder, driverConfig.getOptions());

                                        volumeOptionBuilder.value(driverConfigBuilder.build());
                                    }
                                    mountBuilder.value(volumeOptionBuilder.build());
                                }

                                mountListBuilder.value(mountBuilder.build());
                            }

                            containerBuilder.value(mountListBuilder.build());
                        }

                        addProperty(containerBuilder, containerSpec.getOpenStdin(), "OpenStdin");

                        var privileges = containerSpec.getPrivileges();
                        if (privileges != null) {
                            var privilegeBuilder = new DefaultSubmodelElementCollection.Builder()
                                    .displayName(new DefaultLangStringNameType.Builder().text("Privileges").build());

                            var credentialSpec = privileges.getCredentialSpec();
                            if (credentialSpec != null) {
                                var credentialSpecBuilder = new DefaultSubmodelElementCollection.Builder()
                                        .displayName(new DefaultLangStringNameType.Builder().text("Credential Spec").build());

                                addProperty(credentialSpecBuilder, credentialSpec.getFile(), "File");
                                addProperty(credentialSpecBuilder, credentialSpec.getRegistry(), "Registry");

                                privilegeBuilder.value(credentialSpecBuilder.build());
                            }

                            var seLinuxContext = privileges.getSeLinuxContext();
                            if (seLinuxContext != null) {
                                var seLinuxContextBuilder = new DefaultSubmodelElementCollection.Builder()
                                        .displayName(new DefaultLangStringNameType.Builder().text("SeLinux Context").build());

                                addProperty(seLinuxContextBuilder, seLinuxContext.getRole(), "Role");
                                addProperty(seLinuxContextBuilder, seLinuxContext.getDisable(), "Disable");
                                addProperty(seLinuxContextBuilder, seLinuxContext.getType(), "Type");
                                addProperty(seLinuxContextBuilder, seLinuxContext.getUser(), "User");
                                addProperty(seLinuxContextBuilder, seLinuxContext.getLevel(), "Level");

                                privilegeBuilder.value(seLinuxContextBuilder.build());
                            }

                            containerBuilder.value(privilegeBuilder.build());
                        }

                        addProperty(containerBuilder, containerSpec.getReadOnly(), "ReadOnly");
                        addProperty(containerBuilder, containerSpec.getStopGracePeriod(), "Stop Grace Period");
                        addProperty(containerBuilder, containerSpec.getStopSignal(), "Stop Signal");
                        addProperty(containerBuilder, containerSpec.getTty(), "Tty");
                        addProperty(containerBuilder, containerSpec.getUser(), "User");


                        taskBuilder.value(containerBuilder.build());
                    }

                    var resources = taskTemplate.getResources();
                    if (resources != null) {
                        var resourcesBuilder = new DefaultSubmodelElementCollection.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("Resources").build());

                        var reservations = resources.getReservations();
                        if (reservations != null) {
                            var reservationBuilder = new DefaultSubmodelElementCollection.Builder()
                                    .displayName(new DefaultLangStringNameType.Builder().text("Reservation").build());

                            addProperty(reservationBuilder, reservations.getNanoCPUs(), "Nano CPUs");
                            addProperty(reservationBuilder, reservations.getMemoryBytes(), "Memory Bytes");

                            resourcesBuilder.value(reservationBuilder.build());
                        }

                        var limits = resources.getLimits();
                        if (limits != null) {
                            var limitBuilder = new DefaultSubmodelElementCollection.Builder()
                                    .displayName(new DefaultLangStringNameType.Builder().text("Limits").build());
                            addProperty(limitBuilder, limits.getNanoCPUs(), "Nano CPUs");
                            addProperty(limitBuilder, limits.getMemoryBytes(), "Memory Bytes");

                            resourcesBuilder.value(limitBuilder.build());
                        }

                        taskBuilder.value(resourcesBuilder.build());
                    }

                    var taskNetworks = taskTemplate.getNetworks();
                    if (taskNetworks != null && !taskNetworks.isEmpty()) {
                        var taskNetworksListBuilder = new DefaultSubmodelElementList.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("Networks").build());

                        for (NetworkAttachmentConfig taskNetwork : taskNetworks) {
                            var taskNetworkBuilder = new DefaultSubmodelElementCollection.Builder()
                                    .displayName(new DefaultLangStringNameType.Builder().text("Network").build());

                            addListOfStringProperties(taskNetworkBuilder, taskNetwork.getAliases(), "Aliases", "Alias");
                            addProperty(taskNetworkBuilder, taskNetwork.getTarget(), "Target");

                            taskNetworksListBuilder.value(taskNetworkBuilder.build());
                        }

                        taskBuilder.value(taskNetworksListBuilder.build());
                    }

                    var placement = taskTemplate.getPlacement();
                    if (placement != null) {
                        var placementBuilder = new DefaultSubmodelElementCollection.Builder()
                                .displayName(new DefaultLangStringNameType.Builder().text("Placement").build());

                        addProperty(placementBuilder, placement.getMaxReplicas(), "Max Replicas");
                        addListOfStringProperties(placementBuilder, placement.getConstraints(), "Constraints", "Constraint");

                        var platforms = placement.getPlatforms();
                        if (platforms != null && !platforms.isEmpty()) {
                            var platformsListBuilder = new DefaultSubmodelElementList.Builder()
                                    .displayName(new DefaultLangStringNameType.Builder().text("Platforms").build());

                            for (SwarmNodePlatform platform : platforms) {
                                var platformBuilder = new DefaultSubmodelElementCollection.Builder()
                                        .displayName(new DefaultLangStringNameType.Builder().text("Platform").build());
                                addProperty(platformBuilder, platform.getOs(), "Os");
                                addProperty(platformBuilder, platform.getArchitecture(), "Architecture");

                                platformsListBuilder.value(platformBuilder.build());
                            }

                            placementBuilder.value(platformsListBuilder.build());
                        }

                        taskBuilder.value(placementBuilder.build());
                    }

                    specBuilder.value(taskBuilder.build());
                }

                var updateConfig = spec.getUpdateConfig();
                if (updateConfig != null) {
                    var updateConfigBuilder = new DefaultSubmodelElementCollection.Builder()
                            .displayName(new DefaultLangStringNameType.Builder().text("Update Config").build());

                    addProperty(updateConfigBuilder, updateConfig.getMaxFailureRatio(), "Max Failure Ratio");
                    addProperty(updateConfigBuilder, updateConfig.getMonitor(), "Monitor");
                    addProperty(updateConfigBuilder, updateConfig.getParallelism(), "Parallelism");
                    addProperty(updateConfigBuilder, updateConfig.getDelay(), "Delay");

                    var order = updateConfig.getOrder();
                    if (order != null) {
                        addProperty(updateConfigBuilder, order.name(), "Order");
                    }

                    var failureAction = updateConfig.getFailureAction();
                    if (failureAction != null) {
                        addProperty(updateConfigBuilder, failureAction.name(), "Failure Action");
                    }


                    specBuilder.value(updateConfigBuilder.build());
                }

                secBuilder.value(specBuilder.build());
            }

            secretsListBuilder.value(secBuilder.build());
        }

        return Optional.of(secretsListBuilder);
    }


    private Optional<SubmodelElementCollection> createCollectionValue(String name, DockerObject dockerObject) {

        if (name == null || dockerObject == null) {
            return Optional.empty();
        }

        var collectionBuilder = new DefaultSubmodelElementCollection.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text(name).build());

        var values = dockerObject.getRawValues();
        values.forEach((key, value) -> {

            if (value == null) {
                return;
            }

            if (value instanceof DockerObject v) {
                createCollectionValue(key, v).ifPresent(collectionBuilder::value);
            } else if (value instanceof Collection<?> v) {
                var listValues = v.stream().map(o -> (Object) o).toList();
                if (!listValues.isEmpty()) {
                    createList(key, listValues).ifPresent(collectionBuilder::value);
                }
            } else if (value instanceof Map<?, ?> map) {
                var m = new HashMap<String, Object>();
                map.forEach((k, v) -> {
                    if (k instanceof String keyName) {
                        m.put(keyName, v);
                    }
                });
                createMap(m).ifPresent(collectionBuilder::value);
            } else {
                createProperty(key, value).ifPresent(collectionBuilder::value);
            }

        });


        return Optional.of(collectionBuilder.build());
    }


    private <T> Optional<SubmodelElementList> createList(String name, List<T> values) {

        if (name == null || values == null || values.isEmpty()) {
            return Optional.empty();
        }

        var listBuilder = new DefaultSubmodelElementList.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text(name).build());

        for (var value : values) {
            if (value instanceof DockerObject dockerObject) {
                createCollectionValue(name, dockerObject).ifPresent(listBuilder::value);
            } else if (value instanceof Collection<?> listValue) {
                var listValues = listValue.stream().map(o -> (Object) o).toList();
                createList(listValues).ifPresent(listBuilder::value);
            } else if (value instanceof Map<?, ?> map) {
                var m = new HashMap<String, Object>();
                map.forEach((k, v) -> {
                    if (k instanceof String keyName) {
                        m.put(keyName, v);
                    }
                });
                createMap(m).ifPresent(listBuilder::value);
            } else {
                createProperty(name, value).ifPresent(listBuilder::value);
            }
        }

        return Optional.of(listBuilder.build());
    }

    private <T> Optional<SubmodelElementList> createList(Collection<T> values) {
        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }
        var listBuilder = new DefaultSubmodelElementList.Builder();


        for (var value : values) {
            var className = value.getClass().getName();

            if (value instanceof DockerObject dockerObject) {
                createCollectionValue(className, dockerObject).ifPresent(listBuilder::value);
            } else if (value instanceof Collection<?> listValue) {
                var listValues = listValue.stream().map(o -> (Object) o).toList();
                createList(listValues).ifPresent(listBuilder::value);
            } else if (value instanceof Map<?, ?> map) {
                var m = new HashMap<String, Object>();
                map.forEach((k, v) -> {
                    if (k instanceof String keyName) {
                        m.put(keyName, v);
                    }
                });
                createMap(m).ifPresent(listBuilder::value);
            } else {
                createProperty(value).ifPresent(listBuilder::value);
            }
        }

        return Optional.of(listBuilder.build());
    }

    private <T> Optional<SubmodelElementList> createMap(Map<String, T> map) {
        if (map == null || map.isEmpty()) {
            return Optional.empty();
        }

        var mapBuilder = new DefaultSubmodelElementList.Builder();

        map.forEach((mapKey, mapValue) -> {
            createMapElement(mapKey, mapValue).ifPresent(mapBuilder::value);
        });

        return Optional.of(mapBuilder.build());
    }

    private <T> Optional<SubmodelElementCollection> createMapElement(String name, T value) {
        if (name == null || value == null) {
            return Optional.empty();
        }
        var collectionBuilder = new DefaultSubmodelElementCollection.Builder();
        createProperty(name).ifPresent(collectionBuilder::value);

        if (value instanceof DockerObject dockerObject) {
            createCollectionValue(name, dockerObject).ifPresent(collectionBuilder::value);
        } else if (value instanceof Collection<?> listValue) {
            var listValues = listValue.stream().map(o -> (Object) o).toList();
            createList(listValues).ifPresent(collectionBuilder::value);
        } else if (value instanceof Map<?, ?> map) {
            var m = new HashMap<String, Object>();
            map.forEach((k, v) -> {
                if (k instanceof String keyName) {
                    m.put(keyName, v);
                }
            });
            createMap(m).ifPresent(collectionBuilder::value);
        } else {
            createProperty(value).ifPresent(collectionBuilder::value);
        }

        return Optional.of(collectionBuilder.build());
    }

    private <T> Optional<Property> createProperty(T value) {

        if (value == null) {
            return Optional.empty();
        }

        var property = new DefaultProperty.Builder();
        setValueForProperty(value, property);

        return Optional.of(property.build());
    }

    private <T> Optional<Property> createProperty(String name, T value) {

        if (value == null) {
            return Optional.empty();
        }

        var property = new DefaultProperty.Builder()
                .displayName(new DefaultLangStringNameType.Builder().text(name).build());
        setValueForProperty(value, property);

        return Optional.of(property.build());
    }

    private <T> void setValueForProperty(T value, DefaultProperty.Builder property) {
        if (value instanceof String v) {
            property.valueType(DataTypeDefXsd.STRING).value(v);
        } else if (value instanceof Integer v) {
            property.valueType(DataTypeDefXsd.INTEGER).value(v.toString());
        } else if (value instanceof Long v) {
            property.valueType(DataTypeDefXsd.LONG).value(v.toString());
        } else if (value instanceof Double v) {
            property.valueType(DataTypeDefXsd.DOUBLE).value(v.toString());
        } else if (value instanceof Float v) {
            property.valueType(DataTypeDefXsd.FLOAT).value(v.toString());
        } else if (value instanceof Decimal v) {
            property.valueType(DataTypeDefXsd.DECIMAL).value(v.toString());
        } else if (value instanceof Boolean v) {
            property.valueType(DataTypeDefXsd.BOOLEAN).value(v.toString());
        } else if (value instanceof Date v) {
            property.valueType(DataTypeDefXsd.DATE).value(this.dateFormat.format(v));
        } else if (value instanceof Byte v) {
            property.valueType(DataTypeDefXsd.BYTE).value(v.toString());
        } else if (value instanceof Duration v) {
            property.valueType(DataTypeDefXsd.DURATION).value(v.toString());
        } else if (value instanceof Enum<?> v) {
            property.valueType(DataTypeDefXsd.STRING).value(v.name());
        }
    }


    private void addProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, String value, String name) {
        if (value != null) {
            collectionBuilder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.STRING)
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .value(value)
                    .build());
        }
    }


    private void addDateTimeProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, Date dateTime, String name) {
        if (dateTime != null) {
            collectionBuilder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.DATE_TIME)
                    .value(dateTimeFormat.format(dateTime))
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addDateProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, Date date, String name) {
        if (date != null) {
            collectionBuilder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.DATE)
                    .value(dateFormat.format(date))
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, Integer value, String name) {
        if (value != null) {
            collectionBuilder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.INTEGER)
                    .value(value.toString())
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, int value, String name) {
        collectionBuilder.value(new DefaultProperty.Builder()
                .valueType(DataTypeDefXsd.INTEGER)
                .value(String.valueOf(value))
                .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                .build());
    }

    private void addProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, Long value, String name) {
        if (value != null) {
            collectionBuilder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.LONG)
                    .value(String.valueOf(value))
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addDateTimeProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, Long dateTime, String name) {
        if (dateTime != null) {
            collectionBuilder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.DATE_TIME)
                    .value(dateTimeFormat.format(dateTime))
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, Boolean value, String name) {
        if (value != null) {
            collectionBuilder.value(
                    new DefaultProperty.Builder()
                            .valueType(DataTypeDefXsd.BOOLEAN)
                            .value(value.toString())
                            .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                            .build()
            );
        }
    }

    private void addProperty(DefaultSubmodelElementCollection.Builder collectionBuilder, Float value, String name) {
        if (value != null) {
            collectionBuilder.value(
                    new DefaultProperty.Builder()
                            .valueType(DataTypeDefXsd.FLOAT)
                            .value(value.toString())
                            .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                            .build()
            );
        }
    }


    private void addProperty(DefaultSubmodelElementList.Builder builder, String value, String name) {
        if (value != null) {
            builder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.STRING)
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .value(value)
                    .build());
        }
    }

    private void addDateTimeProperty(DefaultSubmodelElementList.Builder builder, Date dateTime, String name) {
        if (dateTime != null) {
            builder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.DATE_TIME)
                    .value(dateTimeFormat.format(dateTime))
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addDateProperty(DefaultSubmodelElementList.Builder builder, Date date, String name) {
        if (date != null) {
            builder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.DATE)
                    .value(dateFormat.format(date))
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addProperty(DefaultSubmodelElementList.Builder builder, Integer value, String name) {
        if (value != null) {
            builder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.INTEGER)
                    .value(value.toString())
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addProperty(DefaultSubmodelElementList.Builder builder, int value, String name) {
        builder.value(new DefaultProperty.Builder()
                .valueType(DataTypeDefXsd.INTEGER)
                .value(String.valueOf(value))
                .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                .build());
    }

    private void addDateTimeProperty(DefaultSubmodelElementList.Builder builder, Long dateTime, String name) {
        if (dateTime != null) {
            builder.value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.DATE_TIME)
                    .value(dateTimeFormat.format(dateTime))
                    .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                    .build());
        }
    }

    private void addProperty(DefaultSubmodelElementList.Builder builder, Boolean value, String name) {
        if (value != null) {
            builder.value(
                    new DefaultProperty.Builder()
                            .valueType(DataTypeDefXsd.BOOLEAN)
                            .value(value.toString())
                            .displayName(new DefaultLangStringNameType.Builder().text(name).build())
                            .build()
            );
        }
    }


    private void addListOfStringProperties(DefaultSubmodelElementCollection.Builder builder, List<String> values, String listName, String name) {
        if (values != null && !values.isEmpty()) {
            var list = new DefaultSubmodelElementList.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text(listName).build());

            for (var value : values) {
                addProperty(list, value, name);
            }
            builder.value(list.build());
        }
    }

    private void addListOfStringProperties(DefaultSubmodelElementCollection.Builder builder, String[] values, String listName, String name) {
        if (values != null && values.length > 0) {
            var list = new DefaultSubmodelElementList.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text(listName).build());

            for (var value : values) {
                addProperty(list, value, name);
            }
            builder.value(list.build());
        }
    }

    private void addListOfIntegerProperties(DefaultSubmodelElementCollection.Builder builder, List<Integer> values, String listName, String name) {
        if (values != null && !values.isEmpty()) {
            var list = new DefaultSubmodelElementList.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text(listName).build());

            for (var value : values) {

                addProperty(list, value, name);
            }
            builder.value(list.build());
        }
    }


    private void addLabels(DefaultSubmodelElementCollection.Builder containerCollectionBuilder, Map<String, String> labels) {
        if (labels != null && !labels.isEmpty()) {
            var labelsListBuilder = new DefaultSubmodelElementList.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text("Labels").build());

            labels.forEach((key, value) -> {
                var pair = new DefaultSubmodelElementCollection.Builder();
                addProperty(pair, key, "Label");
                addProperty(pair, value, "Value");
                labelsListBuilder.value(pair.build());
            });
            containerCollectionBuilder.value(labelsListBuilder.build());
        }
    }

    private void addOptions(DefaultSubmodelElementCollection.Builder builder, Map<String, String> options) {
        if (options != null && !options.isEmpty()) {
            var networkOptionsBuilder = new DefaultSubmodelElementList.Builder()
                    .displayName(new DefaultLangStringNameType.Builder().text("Options").build());

            options.forEach((key, value) -> {
                var pair = new DefaultSubmodelElementCollection.Builder();
                addProperty(pair, key, "Key");
                addProperty(pair, value, "Value");

                networkOptionsBuilder.value(pair.build());
            });

            builder.value(networkOptionsBuilder.build());
        }
    }

}
