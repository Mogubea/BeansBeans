/*
 * Copyright (C) 2011-2020 lishid. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package me.playground.utils;

import java.util.*;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import me.playground.regions.flags.Flag;
import me.playground.regions.flags.FlagBoolean;
import me.playground.regions.flags.FlagFloat;
import me.playground.regions.flags.FlagMember;
import me.playground.regions.flags.MemberLevel;

/**
 * Utility class for common tab completions.
 */
public class TabCompleter {

    /**
     * Offer tab completions for whole numbers.
     *
     * @param argument the argument to complete
     * @return integer options
     */
    public static List<String> completeInteger(String argument) {
        // Ensure existing argument is actually a number
        if (!argument.isEmpty()) {
            try {
                Integer.parseInt(argument);
            } catch (NumberFormatException e) {
                return Collections.emptyList();
            }
        }

        List<String> completions = new ArrayList<>(10);
        for (int i = 0; i < 10; ++i) {
            completions.add(argument + i);
        }

        return completions;
    }
    
    public static List<String> completeIntegerBetween(String argument, int min, int max) {
        // Ensure existing argument is actually a number
        if (!argument.isEmpty()) {
            try {
                Integer.parseInt(argument);
            } catch (NumberFormatException e) {
                return Collections.emptyList();
            }
        }

        List<String> completions = new ArrayList<>(10);
        for (int i = min; i < (max+1); ++i) {
            completions.add(i+"");
        }

        return completions;
    }

    public static List<String> completeFloat(String argument) {
        // Ensure existing argument is actually a number
        if (!argument.isEmpty()) {
            try {
                Float.parseFloat(argument);
            } catch (NumberFormatException e) {
                return Collections.emptyList();
            }
        }

        List<String> completions = new ArrayList<>(10);
        for (int i = 0; i < 10; ++i) {
            completions.add(argument + i);
        }

        return completions;
    }
    
    /**
     * Offer tab completions for a given Enum.
     *
     * @param argument the argument to complete
     * @param enumClazz the Enum to complete for
     * @return the matching Enum values
     */
    public static List<String> completeEnum(String argument, Class<? extends Enum<?>> enumClazz) {
        argument = argument.toLowerCase(Locale.ENGLISH);
        List<String> completions = new ArrayList<>();

        for (Enum<?> enumConstant : enumClazz.getEnumConstants()) {
            String name = enumConstant.name().toLowerCase();
            if (name.contains(argument)) {
                completions.add(name);
            }
        }

        return completions;
    }

    /**
     * Offer tab completions for a given array of Strings.
     *
     * @param argument the argument to complete
     * @param options the Strings which may be completed
     * @return the matching Strings
     */
    public static List<String> completeString(String argument, String[] options) {
        argument = argument.toLowerCase(Locale.ENGLISH);
        List<String> completions = new ArrayList<>();

        for (String option : options) {
            if (option.startsWith(argument)) {
                completions.add(option);
            }
        }

        return completions;
    }
    
    public static List<String> completeString(String argument, Collection<String> options) {
        argument = argument.toLowerCase(Locale.ENGLISH);
        List<String> completions = new ArrayList<>();

        for (String option : options) {
            if (option.startsWith(argument)) {
                completions.add(option);
            }
        }

        return completions;
    }

    /**
     * Offer tab completions for visible online Players' names.
     *
     * @param sender the command's sender
     * @param argument the argument to complete
     * @return the matching Players' names
     */
    public static List<String> completeOnlinePlayer(CommandSender sender, String argument) {
        List<String> completions = new ArrayList<>();
        Player senderPlayer = sender instanceof Player ? (Player) sender : null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (senderPlayer != null && !senderPlayer.canSee(player)) {
                continue;
            }

            if (StringUtil.startsWithIgnoreCase(player.getName(), argument)) {
                completions.add(player.getName());
            }
        }

        return completions;
    }

    /**
     * Offer tab completions for a given array of Objects.
     *
     * @param argument the argument to complete
     * @param converter the Function for converting the Object into a comparable String
     * @param options the Objects which may be completed
     * @return the matching Strings
     */
    public static <T> List<String> completeObject(String argument, Function<T, String> converter, T[] options) {
        argument = argument.toLowerCase(Locale.ENGLISH);
        List<String> completions = new ArrayList<>();

        for (T option : options) {
            String optionString = converter.apply(option).toLowerCase();
            if (optionString.contains(argument)) {
                completions.add(optionString);
            }
        }

        return completions;
    }
    
    public static <T> List<String> completeObject(String argument, Function<T, String> converter, List<T> options) {
        argument = argument.toLowerCase(Locale.ENGLISH);
        List<String> completions = new ArrayList<>();

        for (T option : options) {
            String optionString = converter.apply(option).toLowerCase();
            if (optionString.contains(argument)) {
                completions.add(optionString);
            }
        }

        return completions;
    }

    public static List<String> completeFlagParse(String argument, Flag<?> flag) {
    	List<String> completions = new ArrayList<>();
    	
    	if (flag instanceof FlagMember)
        	completions = completeEnum(argument, MemberLevel.class);
        
    	else if (flag instanceof FlagFloat)
        	completions = completeFloat(argument);
    	
    	else if (flag instanceof FlagBoolean) {
        	completions.add("true");
        	completions.add("false");
        }
    	
    	
        
        completions.add("null");

        return completions;
    }
    
    /**
     * Ignores all LEGACY materials
     */
    public static List<String> completeItems(String argument) {
        argument = argument.toLowerCase(Locale.ENGLISH);
        List<String> completions = new ArrayList<>();

        for (Material enumConstant : Material.values()) {
            String name = enumConstant.name().toLowerCase();
            if (name.startsWith("legacy"))
            	continue;
            if (name.contains(argument))
                completions.add(name);
        }

        return completions;
    }
    
    private TabCompleter() {}

}