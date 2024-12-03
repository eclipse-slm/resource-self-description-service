package org.eclipse.slm.self_description_service.aas;


import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.basyx.core.exceptions.*;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationSupport;
import org.eclipse.digitaltwin.basyx.submodelservice.value.PropertyValue;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelElementValue;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SubmodelValueOnly;
import org.eclipse.slm.self_description_service.datasource.Datasource;
import org.eclipse.slm.self_description_service.datasource.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class SubmodelRepository implements org.eclipse.digitaltwin.basyx.submodelrepository.SubmodelRepository {
    private final static Logger LOG = LoggerFactory.getLogger(SubmodelRepository.class);


    private final HashMap<String, Datasource> submodelToFactoryMap = new HashMap<>();
    private final HashMap<String, Datasource> factories = new HashMap<>();


    @Autowired
    public SubmodelRepository(Template templateFactory) {

        var modelIds = templateFactory.getModelIds();
        for (var id : modelIds) {
            this.submodelToFactoryMap.put(id, templateFactory);
        }

        this.factories.put("Template", templateFactory);

    }

    @Override
    public CursorResult<List<Submodel>> getAllSubmodels(PaginationInfo pInfo) {
        var submodels = new ArrayList<Submodel>();
        for (var submodelFactory : factories.values()) {
            submodels.addAll(submodelFactory.getModels());
        }

        TreeMap<String, Submodel> submodelMap = submodels.stream().collect(Collectors.toMap(Submodel::getId, submodel -> submodel, (a, b) -> a, TreeMap::new));
        PaginationSupport<Submodel> paginationSupport = new PaginationSupport<>(submodelMap, Submodel::getId);

        return paginationSupport.getPaged(pInfo);
    }

    @Override
    public Submodel getSubmodel(String submodelId) throws ElementDoesNotExistException {

        if (!this.submodelToFactoryMap.containsKey(submodelId)) {
            for (Datasource factory : factories.values()) {
                try {
                    var submodel = factory.getModelById(submodelId);
                    if (submodel.isPresent()) {
                        this.submodelToFactoryMap.put(submodelId, factory);
                        return submodel.get();
                    }
                } catch (IOException e) {
                    throw new ElementDoesNotExistException();
                }
            }
        } else {
            var factory = this.submodelToFactoryMap.get(submodelId);
            try {
                var submodel = factory.getModelById(submodelId);
                if (submodel.isPresent()) {
                    return submodel.get();
                }
            } catch (IOException e) {
                throw new ElementDoesNotExistException();
            }
        }


        return null;
    }

    @Override
    public void updateSubmodel(String submodelId, Submodel submodel) throws ElementDoesNotExistException {

    }

    @Override
    public void createSubmodel(Submodel submodel) throws CollidingIdentifierException, MissingIdentifierException {

    }

    @Override
    public void updateSubmodelElement(String submodelIdentifier, String idShortPath, SubmodelElement submodelElement) throws ElementDoesNotExistException {

    }

    @Override
    public void deleteSubmodel(String submodelId) throws ElementDoesNotExistException {

    }

    @Override
    public CursorResult<List<SubmodelElement>> getSubmodelElements(String submodelId, PaginationInfo pInfo) throws ElementDoesNotExistException {

        var submodel = this.getSubmodel(submodelId);

        TreeMap<String, SubmodelElement> submodelMap = submodel.getSubmodelElements().stream()
                .collect(Collectors.toMap(SubmodelElement::getIdShort, submodelElement -> submodelElement, (a, b) -> a, TreeMap::new));
        PaginationSupport<SubmodelElement> paginationSupport = new PaginationSupport<>(submodelMap, SubmodelElement::getIdShort);

        return paginationSupport.getPaged(pInfo);
    }

    @Override
    public SubmodelElement getSubmodelElement(String submodelId, String smeIdShort) throws ElementDoesNotExistException {

        var submodel = this.getSubmodel(submodelId);
        var submodelElement = submodel.getSubmodelElements().stream().filter(smElement -> smElement.getIdShort().equals(smeIdShort)).findFirst();

        return submodelElement.orElse(null);
    }

    @Override
    public SubmodelElementValue getSubmodelElementValue(String submodelId, String smeIdShort) throws ElementDoesNotExistException {
        var submodel = this.getSubmodel(submodelId);
        var submodelElement = submodel.getSubmodelElements().stream()
                .filter(smElement -> smElement.getIdShort().equals(smeIdShort)).findFirst();

        return submodelElement.map(element -> new PropertyValue(((Property) element).getValue())).orElse(null);

    }

    @Override
    public void setSubmodelElementValue(String submodelId, String smeIdShort, SubmodelElementValue value) throws ElementDoesNotExistException {

    }

    @Override
    public void createSubmodelElement(String submodelId, SubmodelElement smElement) {

    }

    @Override
    public void createSubmodelElement(String submodelId, String idShortPath, SubmodelElement smElement) throws ElementDoesNotExistException {

    }

    @Override
    public void deleteSubmodelElement(String submodelId, String idShortPath) throws ElementDoesNotExistException {

    }

    @Override
    public OperationVariable[] invokeOperation(String submodelId, String idShortPath, OperationVariable[] input) throws ElementDoesNotExistException {
        return new OperationVariable[0];
    }

    @Override
    public SubmodelValueOnly getSubmodelByIdValueOnly(String submodelId) throws ElementDoesNotExistException {
        return null;
    }

    @Override
    public Submodel getSubmodelByIdMetadata(String submodelId) throws ElementDoesNotExistException {
        return null;
    }

    @Override
    public File getFileByPathSubmodel(String submodelId, String idShortPath) throws ElementDoesNotExistException, ElementNotAFileException, FileDoesNotExistException {
        return null;
    }

    @Override
    public void setFileValue(String submodelId, String idShortPath, String fileName, InputStream inputStream) throws ElementDoesNotExistException, ElementNotAFileException {

    }

    @Override
    public void deleteFileValue(String submodelId, String idShortPath) throws ElementDoesNotExistException, ElementNotAFileException, FileDoesNotExistException {

    }

    @Override
    public void patchSubmodelElements(String submodelId, List<SubmodelElement> submodelElementList) {

    }

    @Override
    public InputStream getFileByFilePath(String submodelId, String filePath) {
        return null;
    }
}
