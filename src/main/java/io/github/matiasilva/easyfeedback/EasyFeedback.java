package io.github.matiasilva.easyfeedback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.md_5.bungee.api.ChatColor;

public class EasyFeedback extends JavaPlugin {

	@Override
	public void onEnable() {
		if (!getDataFolder().exists())
			getDataFolder().mkdir();
		saveDefaultConfig();

		File toWrite = new File(getDataFolder(), "data.json");
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		if (!toWrite.exists()) {
			try {
				toWrite.createNewFile();
				FileWriter writer = new FileWriter(toWrite);
				String[] test = { "I think this is a test!" };
				writer.write(gson.toJson(test));
				writer.close();
				getLogger().info("Successfully created data.json");
			} catch (Exception e) {
				getLogger().info("An error occurred when creating new file");
			}
		}
	}

	// gets called whenever a player sends a command in our plugin.yml file
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// has the sender called the right command?
		if (command.getName().equalsIgnoreCase("feedback")) {
			File dataFile = new File(getDataFolder(), "data.json");
			if (dataFile.exists())
				getLogger().info("Found the file! Now using it...");
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();
			// is the sender a player or the console?
			if (sender instanceof Player) {
				// cast the sender to a player
				Player p = (Player) sender;
				// has the player specified any arguments?
				if (args.length == 0) {
					// player is requesting to read, check perms
					if (p.hasPermission("EasyFeedback.feedback.read")) {
						p.sendMessage(ChatColor.RED.toString() + "====DISPLAYING ALL FEEDBACK====");
						// try and read the file
						try {
							BufferedReader reader = new BufferedReader(new FileReader(dataFile));
							String[] data = gson.fromJson(reader, String[].class);
							// send the player all the data in the file
							p.sendMessage(data);
							p.sendMessage(ChatColor.GREEN.toString()
									+ String.format("Read  %d entries successfully", data.length));
						} catch (Exception e) {
							// basic exception handling
							p.sendMessage(ChatColor.RED.toString() + "oops...could not find file");
						}
						return true;
					}
				} else {
					// do they have permission to write?
					if (p.hasPermission("EasyFeedback.feedback.write")) {
						// player is sending some feedback, handle it here
						String output = String.join(" ", args);
						// first read the data and serialize as POJO
						try {
							BufferedReader reader = new BufferedReader(new FileReader(dataFile));
							List<String> data = gson.fromJson(reader, new TypeToken<List<String>>() {
							}.getType());
							// add the user's feedback
							data.add(output);
							// now try and write to file
							FileWriter writer = new FileWriter(dataFile);
							writer.write(gson.toJson(data));
							writer.close();
							p.sendMessage(ChatColor.GREEN.toString()
									+ "Recebemos a sua mensagem! Obrigado e continua a jogar :D");
						} catch (FileNotFoundException e) {
							// basic error handling
							p.sendMessage("oops...file was not found");
						} catch (Exception e) {
							p.sendMessage("ocorreu um erro...");
						}
						return true;
					} else {
						// oops, user tried to write but didn't have permission
						p.sendMessage("You have insufficient permissions..");
						return true;
					}
				}
			} else {
				// sender is console, ignore any attempts to write
				if (args.length == 0) {
					// read the data
					sender.sendMessage(ChatColor.RED.toString() + "====DISPLAYING ALL FEEDBACK====");
					try {
						BufferedReader reader = new BufferedReader(new FileReader(dataFile));
						String[] data = gson.fromJson(reader, String[].class);
						sender.sendMessage(data);
						sender.sendMessage(ChatColor.GREEN.toString()
								+ String.format("Read  %d entries successfully", data.length));
					} catch (Exception e) {
						sender.sendMessage(ChatColor.RED.toString() + "oops...could not find file");
					}
					return true;
				}
			}
			return false;
		}
		return false;
	}

}
