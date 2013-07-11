package edu.cmu.side.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.cmu.side.model.data.DocumentList;
import edu.cmu.side.model.data.FeatureTable;
import edu.cmu.side.model.data.PredictionResult;
import edu.cmu.side.model.data.TrainingResult;
import edu.cmu.side.plugin.FeaturePlugin;
import edu.cmu.side.plugin.LearningPlugin;
import edu.cmu.side.plugin.RestructurePlugin;
import edu.cmu.side.plugin.SIDEPlugin;
import edu.cmu.side.plugin.WrapperPlugin;

public class Recipe implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	RecipeManager.Stage stage = null;
	private String recipeName = "";
	OrderedPluginMap extractors;
	OrderedPluginMap filters;
	OrderedPluginMap wrappers;
	LearningPlugin learner;
	Map<String, String> learnerSettings;
	Map<String, Serializable> validationSettings;

	DocumentList documentList;
	FeatureTable featureTable;
	FeatureTable filteredTable;
	TrainingResult trainedModel;
	PredictionResult predictionResult;
	
	public RecipeManager.Stage getStage()
	{
		if (stage == null)
		{
			if (predictionResult != null)
			{
				stage = RecipeManager.Stage.PREDICTION_RESULT;
			}
			else if (trainedModel != null)
			{
				stage = RecipeManager.Stage.TRAINED_MODEL;
			}
			else if (learnerSettings != null && learnerSettings.containsKey("classifier"))
			{
				stage = RecipeManager.Stage.PREDICTION_ONLY;
			}
			else if (filteredTable != null)
			{
				stage = RecipeManager.Stage.MODIFIED_TABLE;
			}
			else if (featureTable != null)
			{
				stage = RecipeManager.Stage.FEATURE_TABLE;
			}
			else if (documentList != null)
			{
				stage = RecipeManager.Stage.DOCUMENT_LIST;
			}
			else
				stage = RecipeManager.Stage.NONE;

		}
		return stage;
	}

	public void resetStage(){
		stage = null;
	}

	public String toString()
	{
		String out = "";
		if (RecipeManager.Stage.DOCUMENT_LIST.equals(stage))
		{
			out = documentList.getName();
		}
		else if (RecipeManager.Stage.FEATURE_TABLE.equals(stage))
		{
			out = featureTable.getName();
		}
		else if (RecipeManager.Stage.MODIFIED_TABLE.equals(stage))
		{
			out = filteredTable.getName();
		}
		else if (RecipeManager.Stage.TRAINED_MODEL.equals(stage))
		{
			out = trainedModel.getName();
		}
		else
		{
			out = ""+stage;
		}
		if (out == null) out = stage.toString();
		return out;
	}

	//Filtered tables may alter the document list being worked with.
	public DocumentList getDocumentList(){ 
		return filteredTable == null ? documentList : filteredTable.getDocumentList();
	}

	public FeatureTable getFeatureTable(){ return featureTable; }
	
	public FeatureTable getFilteredTable(){ return filteredTable; }
	
	public FeatureTable getTrainingTable(){
		if(filteredTable == null) return getFeatureTable(); else return getFilteredTable();
	}
	
	public TrainingResult getTrainingResult(){ return trainedModel; }
	
	public PredictionResult getPredictionResult(){ return predictionResult; }

	public OrderedPluginMap getExtractors(){ return extractors; }
	
	public OrderedPluginMap getFilters(){ return filters; }
	
	public OrderedPluginMap getWrappers(){ return wrappers; }
	
	public LearningPlugin getLearner(){ return learner; }

	public void setDocumentList(DocumentList sdl){
		documentList = sdl;
		resetStage();
	}

	public void setFeatureTable(FeatureTable ft){
		featureTable = ft;
		resetStage();
	}

	public void setFilteredTable(FeatureTable ft){
		filteredTable = ft;
		resetStage();
	}
	
	public void setTrainingResult(TrainingResult tm){
		trainedModel = tm;
		resetStage();
	}
	
	public void setPredictionResult(PredictionResult pr){
		predictionResult = pr;
		resetStage();
	}
	
	public void addExtractor(FeaturePlugin plug, Map<String, String> settings){
		if(settings == null)
			settings = plug.generateConfigurationSettings();
		extractors.put(plug, settings);
		resetStage();
	}
	
	public void addFilter(RestructurePlugin plug, Map<String, String> settings){
		if(settings == null)
			settings = plug.generateConfigurationSettings();
		filters.put(plug, settings);
		resetStage();
	}
	
	public void addWrapper(WrapperPlugin plug, Map<String, String> settings){
		if(settings == null)
			settings = plug.generateConfigurationSettings();
		wrappers.put(plug, settings);
		resetStage();
	}
	
	public void setLearner(LearningPlugin plug){
		learner = plug;
		resetStage();
	}
	
	public void setLearnerSettings(Map<String, String> settings){
		learnerSettings = settings;
	}
	
	public Map<String, String> getLearnerSettings(){
		return learnerSettings;
	}
	
	public Map<String, Serializable> getValidationSettings()
	{
		return validationSettings;
	}

	public void setValidationSettings(Map<String, Serializable> validationSettings)
	{
		this.validationSettings = validationSettings;
	}

	private Recipe()
	{
		extractors = new OrderedPluginMap();
		filters= new OrderedPluginMap();
		wrappers = new OrderedPluginMap();
		getStage();
	}

	public static Recipe fetchRecipe(){
		return new Recipe();
	}

	public static Recipe addPluginsToRecipe(Recipe prior, Collection<? extends SIDEPlugin> next){
		RecipeManager.Stage stage = prior.getStage();
		Recipe newRecipe = fetchRecipe();
		if(stage.equals(RecipeManager.Stage.DOCUMENT_LIST)){
			addFeaturePlugins(prior, newRecipe, (Collection<FeaturePlugin>)next);
		}else if(stage.equals(RecipeManager.Stage.FEATURE_TABLE) || stage.equals(RecipeManager.Stage.MODIFIED_TABLE)){
			addRestructurePlugins(prior, newRecipe, (Collection<RestructurePlugin>)next);
		}
		return newRecipe;
	}
	
	public static Recipe addLearnerToRecipe(Recipe prior, LearningPlugin next, Map<String, String> settings){
		RecipeManager.Stage stage = prior.getStage();
		Recipe newRecipe = fetchRecipe();
		newRecipe.setDocumentList(prior.getDocumentList());
		for(SIDEPlugin plugin : prior.getExtractors().keySet()){
			newRecipe.addExtractor((FeaturePlugin)plugin, prior.getExtractors().get(plugin));
		}
		newRecipe.setFeatureTable(prior.getFeatureTable());
		for(SIDEPlugin plugin : prior.getFilters().keySet()){
			newRecipe.addFilter((RestructurePlugin)plugin, prior.getFilters().get(plugin));
		}
		newRecipe.setFilteredTable(prior.getFilteredTable());
		for(SIDEPlugin plugin : prior.getWrappers().keySet()){
			newRecipe.addWrapper((WrapperPlugin)plugin, prior.getWrappers().get(plugin));
		}
		newRecipe.setLearner(next);
		newRecipe.setLearnerSettings(settings);
		return newRecipe;
	}
	
	public static Recipe copyPredictionRecipe(Recipe prior)
	{
		Recipe newRecipe = fetchRecipe();
		
		Map<String, List<String>> textColumns = new HashMap<String, List<String>>();
		Map<String, List<String>> columns = new HashMap<String, List<String>>();
		DocumentList originalDocs = prior.getDocumentList();
		
		List<String> emptyList = new ArrayList<String>(0);
		for(String key : originalDocs.getCoveredTextList().keySet())
		{
			textColumns.put(key, emptyList);
		}
		for(String key : originalDocs.allAnnotations().keySet())
		{
			columns.put(key, emptyList);
		}
		
		DocumentList newDocs = new DocumentList(emptyList, textColumns, columns, prior.getFeatureTable().getAnnotation());
		newDocs.setLabelArray(prior.getFeatureTable().getLabelArray());
		
		
		newRecipe.setDocumentList(newDocs);
		
		for (SIDEPlugin plugin : prior.getExtractors().keySet())
		{
			newRecipe.addExtractor((FeaturePlugin) plugin, prior.getExtractors().get(plugin));
		}
		
		FeatureTable dummyTable = prior.getTrainingTable().predictionClone();
		
		newRecipe.setFeatureTable(dummyTable);
		
		for (SIDEPlugin plugin : prior.getFilters().keySet())
		{
			newRecipe.addFilter((RestructurePlugin) plugin, prior.getFilters().get(plugin));
		}
		
		for (SIDEPlugin plugin : prior.getWrappers().keySet()){
			newRecipe.addWrapper((WrapperPlugin) plugin, prior.getWrappers().get(plugin));
		}
		newRecipe.setLearner(prior.getLearner());
		newRecipe.setLearnerSettings(prior.getLearnerSettings());
		newRecipe.setValidationSettings(prior.getValidationSettings());
		newRecipe.setRecipeName(prior.getRecipeName());

		return newRecipe;
	}
	
	public static Recipe copyEmptyRecipe(Recipe prior)
	{
		RecipeManager.Stage stage = prior.getStage();
		Recipe newRecipe = fetchRecipe();
//		newRecipe.setDocumentList(prior.getDocumentList());
		for (SIDEPlugin plugin : prior.getExtractors().keySet())
		{
			newRecipe.addExtractor((FeaturePlugin) plugin, prior.getExtractors().get(plugin));
		}
//		newRecipe.setFeatureTable(prior.getFeatureTable());
		for (SIDEPlugin plugin : prior.getFilters().keySet())
		{
			newRecipe.addFilter((RestructurePlugin) plugin, prior.getFilters().get(plugin));
		}
		
		for (SIDEPlugin plugin : prior.getWrappers().keySet()){
			newRecipe.addWrapper((WrapperPlugin) plugin, prior.getWrappers().get(plugin));
		}
//		newRecipe.setFilteredTable(prior.getFilteredTable());
		newRecipe.setLearner(prior.getLearner());
		newRecipe.setLearnerSettings(prior.getLearnerSettings());
		newRecipe.setValidationSettings(prior.getValidationSettings());

		return newRecipe;
	}
	
	protected static void addFeaturePlugins(Recipe prior, Recipe newRecipe, Collection<FeaturePlugin> next){
		newRecipe.setDocumentList(prior.getDocumentList());
		for(FeaturePlugin plugin : next){
			assert next instanceof FeaturePlugin;
			newRecipe.addExtractor(plugin, prior.getExtractors().get(plugin));
		}
	}
	
	protected static void addRestructurePlugins(Recipe prior, Recipe newRecipe, Collection<RestructurePlugin> next){
		newRecipe.setDocumentList(prior.getDocumentList());
		for (SIDEPlugin plugin : prior.getExtractors().keySet())
		{
			newRecipe.addExtractor((FeaturePlugin) plugin, prior.getExtractors().get(plugin));
		}
		newRecipe.setFeatureTable(prior.getTrainingTable());
		
		for (RestructurePlugin plugin : next)
		{
			assert next instanceof RestructurePlugin;
			newRecipe.addFilter(plugin, plugin.generateConfigurationSettings());
		}
	}

	public String getRecipeName()
	{
		if(recipeName.isEmpty())
		{
			String out = "";
			if(RecipeManager.Stage.DOCUMENT_LIST.equals(stage)){
				out = documentList.getName();
			}else if(RecipeManager.Stage.FEATURE_TABLE.equals(stage)){
				out = featureTable.getName();
			}else if(RecipeManager.Stage.MODIFIED_TABLE.equals(stage)){
				out = filteredTable.getName();
			}else if(RecipeManager.Stage.TRAINED_MODEL.equals(stage)){
				out = trainedModel.getName();
			}else{
				out = ""+stage;
			}
			return out+"."+stage.extension;
		}
		return recipeName;
	}
	public void setRecipeName(String recipeName)
	{
		this.recipeName = recipeName;
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		System.out.println("reading "+this + " from "+in);
		stage = (RecipeManager.Stage) in.readObject();
		recipeName = (String) in.readObject();
		extractors = (OrderedPluginMap) in.readObject();
		filters = (OrderedPluginMap) in.readObject();
		wrappers = (OrderedPluginMap) in.readObject();
		learner = (LearningPlugin) SIDEPlugin.fromSerializable((Serializable) in.readObject()); //it's all for you!
		learnerSettings = (Map<String, String>) in.readObject();
		documentList = (DocumentList) in.readObject();
		featureTable = (FeatureTable) in.readObject();
		filteredTable = (FeatureTable) in.readObject();
		trainedModel = (TrainingResult)in.readObject();
		predictionResult = (PredictionResult) in.readObject();
		validationSettings = (Map<String, Serializable>) in.readObject();
	}

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		System.out.println("writing "+this + " to "+out);
		out.writeObject(stage);
		out.writeObject(recipeName);
		out.writeObject(extractors);
		out.writeObject(filters);
		out.writeObject(wrappers);
		out.writeObject(learner==null?null:learner.toSerializable());
		out.writeObject(learnerSettings);
		out.writeObject(documentList);
		out.writeObject(featureTable);
		out.writeObject(filteredTable);
		out.writeObject(trainedModel);
		out.writeObject(predictionResult);
		out.writeObject(validationSettings);
	}

	public void saveToXML(Document doc) {
		//First, write the Recipe parent
		Element recipe = doc.createElement("Recipe");
		doc.appendChild(recipe);
		//Next, we write out the stage
		Attr stage = doc.createAttribute("Stage");
		stage.setNodeValue(stage.getValue());
		recipe.setAttributeNode(stage);
		//writing out recipeName
		Attr name = doc.createAttribute("Recipe Name");
		name.setNodeValue(recipeName);
		recipe.setAttributeNode(name);
		//Writing out plugins
		Element extractorsElement = doc.createElement("Extractors");
		recipe.appendChild(extractorsElement);
		//extractors.saveToXML(doc, extractorsElement);
		Element filtersElement = doc.createElement("Filters");
		recipe.appendChild(filtersElement);
		//filters.saveToXML(doc, filtersElement);
		Element wrappersElement = doc.createElement("Wrappers");
		recipe.appendChild(wrappersElement);
		//wrappers.saveToXML(doc, wrappersElement);
		
		//Writing learner
		Element learnerElement = doc.createElement("Learner");
		recipe.appendChild(learnerElement);
		//learner.saveToXML(doc, learnerElement);
		
		//Writing learner settings
		Element learnerSettingsElement = doc.createElement("Learner Settings");
		learnerElement.appendChild(learnerSettingsElement);
		Attr attrSetting;
		for (String setting : learnerSettings.keySet()) {
			attrSetting = doc.createAttribute(setting);
			learnerSettingsElement.setAttribute(setting, learnerSettings.get(setting));
		}
		
		//Writing DocumentList
		Element documentListElement = doc.createElement("Document List");
		recipe.appendChild(documentListElement);
		//documentList.writeToXML(doc, documentListElement);
		
		//Writing FeatureTable and filtertable
		Element featureTableElement = doc.createElement("Feature Table");
		recipe.appendChild(featureTableElement);
		//featureTable.writeToXML(doc, featureTableElement);
		
		Element filteredTableElement = doc.createElement("Filtered Table");
		recipe.appendChild(filteredTableElement);
		//filteredTable.writeToXML(doc, filteredTableElement);
		
		//TrainedModel
		Element trainedModelElement = doc.createElement("Trained Model");
		recipe.appendChild(trainedModelElement);
		//trainedModel.writeToXML(doc, trainedModelElement);
		
		//PredictionResults
		Element predictionResultsElement = doc.createElement("Prediction Results");
		recipe.appendChild(predictionResultsElement);
		//predictionResult.writeToXML(doc, predictionResultsElement);

		//ValidationSettings
		Element validationSettingsElement = doc.createElement("Validation Settings");
		recipe.appendChild(validationSettingsElement);
		//We have to serialize the map
	}
}