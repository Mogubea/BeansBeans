package me.playground.civilizations.structures;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import me.playground.civilizations.Civilization;
import me.playground.civilizations.CivilizationTier;
import me.playground.utils.BeanColor;
import me.playground.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class Structures {
	
	private static final Map<String, Structures> structures = new LinkedHashMap<String, Structures>();
	private static final Map<Integer, Structures> structuresId = new LinkedHashMap<Integer, Structures>();
	
	public static Structures getStructure(String name) {
		return structures.get(name.toLowerCase());
	}
	
	public static Structures getStructure(int id) {
		return structuresId.get(id);
	}
	
	private final int id;
	private final String name;
	private final Component component;
	private final int cost;
	
	private final CivilizationTier requiredTier;
	private final Set<Structures> requiredStructures = new HashSet<Structures>();
	
	public Structures(int id, String name, int cost, String description, JSONObject data) {
		this.id = id;
		this.name = name.toLowerCase();
		this.cost = cost;
		this.component = Component.text(Utils.firstCharUpper(name), BeanColor.STRUCTURE).decoration(TextDecoration.ITALIC, false);
		
		CivilizationTier attempt = CivilizationTier.HAMLET;
		
		if (data != null) {
			try { attempt = CivilizationTier.valueOf(data.optString("tier").toUpperCase()); } catch (Exception e) { }
			
			JSONArray array = data.optJSONArray("structures");
			if (array != null) {
				int size = array.length();
				for (int x = -1; ++x < size;) {
					Structures structure = getStructure(array.optString(x));
					if (structure != null) requiredStructures.add(structure);
				}
			}
		}
		this.requiredTier = attempt;
		
		structures.put(name.toLowerCase(), this);
		structuresId.put(id, this);
	}
	
	public int getCost(Civilization c) {
		return this.cost;
	}
	
	public int getBaseCost() {
		return this.cost;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getId() {
		return this.id;
	}
	
	public CivilizationTier getRequiredTier() {
		return requiredTier;
	}
	
	public Set<Structures> getRequiredStructures() {
		return requiredStructures;
	}
	
	public Component toComponent() {
		return component;
	}
	
}
