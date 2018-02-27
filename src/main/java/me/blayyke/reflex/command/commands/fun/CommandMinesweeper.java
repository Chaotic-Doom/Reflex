package me.blayyke.reflex.command.commands.fun;

import me.blayyke.reflex.Colours;
import me.blayyke.reflex.command.AbstractCommand;
import me.blayyke.reflex.command.CommandCategory;
import me.blayyke.reflex.command.CommandContext;
import me.blayyke.reflex.game.MineSweeperGame;
import me.blayyke.reflex.game.MineSweeperManager;
import me.blayyke.reflex.utils.MiscUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;

public class CommandMinesweeper extends AbstractCommand {
    @Override
    public CommandCategory getCategory() {
        return CommandCategory.DEVELOPER;
    }

    @Override
    public String getName() {
        return "minesweeper";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"ms"};
    }

    @Override
    public String getDesc() {
        return "Play a game of minesweeper";
    }

    @Override
    public void onCommand(CommandContext context) {
        MineSweeperManager manager = getReflex().getDataManager().getGuildStorage(context.getGuild()).getMineSweeperManager();
        EmbedBuilder embedBuilder = createEmbed();
        embedBuilder.setTitle("Minesweeper");

        if (!manager.userHasGame(context.getMember().getUser())) {
            if (context.getArgs().length < 2) {
                embedBuilder.setColor(Colours.WARN);

                embedBuilder.setDescription("Missing required args: <board size> <mine chance>");
                context.getChannel().sendMessage(embedBuilder.build()).queue();
                return;
            }
            if (!MiscUtils.isId(context.getArgs()[0])) {
                embedBuilder.setColor(Colours.WARN);

                embedBuilder.setDescription("Argument <1 / board size> must be a number!");
                context.getChannel().sendMessage(embedBuilder.build()).queue();
                return;
            }
            if (!MiscUtils.isDouble(context.getArgs()[1])) {
                embedBuilder.setColor(Colours.WARN);

                embedBuilder.setDescription("Argument <2 / mine chance> must be a decimal number!");
                context.getChannel().sendMessage(embedBuilder.build()).queue();
                return;
            }
            int boardSize = Integer.parseInt(context.getArgs()[0]);
            double mineChance = Double.parseDouble(context.getArgs()[1]);

            if (boardSize > MineSweeperManager.MAX_BOARD_SIZE || boardSize < MineSweeperManager.MIN_BOARD_SIZE) {
                embedBuilder.setColor(Colours.WARN);

                embedBuilder.setDescription("Board size must be between " + MineSweeperManager.MIN_BOARD_SIZE + " and " + MineSweeperManager.MAX_BOARD_SIZE + ".");
                context.getChannel().sendMessage(embedBuilder.build()).queue();
                return;
            }
            if (mineChance > MineSweeperManager.MAX_MINE_CHANCE || mineChance < MineSweeperManager.MIN_MINE_CHANCE) {
                embedBuilder.setColor(Colours.WARN);

                embedBuilder.setDescription("Mine chance size must be between " + MineSweeperManager.MIN_MINE_CHANCE + " and " + MineSweeperManager.MAX_MINE_CHANCE + ".");
                context.getChannel().sendMessage(embedBuilder.build()).queue();
                return;
            }

            Message message = context.getChannel().sendMessage("Creating game...").complete();
            MineSweeperGame game = manager.createGame(context.getMessage().getAuthor(), message);
            game.startGame(boardSize, mineChance);
            message.editMessage("Game created:\n\n" + game.getBlankBoard()).queue();
            return;
        }
        MineSweeperGame game = getReflex().getDataManager().getGuildStorage(context.getGuild()).getMineSweeperManager().getGame(context.getMember().getUser());

        if (context.getArgs().length < 2) {
            embedBuilder.setColor(Colours.WARN);

            embedBuilder.setDescription("Missing required args: <x> <y> [flag]");
            context.getChannel().sendMessage(embedBuilder.build()).queue();
            return;
        }

        if (!MiscUtils.isId(context.getArgs()[0])) {
            embedBuilder.setColor(Colours.WARN);

            embedBuilder.setDescription("Argument <1 / x-coordinate> must be a number!");
            context.getChannel().sendMessage(embedBuilder.build()).queue();
            return;
        }
        if (!MiscUtils.isId(context.getArgs()[1])) {
            embedBuilder.setColor(Colours.WARN);

            embedBuilder.setDescription("Argument <2 / y-coordinate> must be a number!");
            context.getChannel().sendMessage(embedBuilder.build()).queue();
            return;
        }

        int inputX = Integer.parseInt(context.getArgs()[0]);
        int inputY = Integer.parseInt(context.getArgs()[1]);

        if (context.getArgs().length > 2) {
            if (!context.getArgs()[2].equalsIgnoreCase("flag")) {
                embedBuilder.setColor(Colours.WARN);

                embedBuilder.setDescription("Argument <3 / string> must equal `flag`!");
                context.getChannel().sendMessage(embedBuilder.build()).queue();
                return;
            }
            game.flagInput(context.getChannel(), inputX, inputY);
        } else
            game.input(context.getChannel(), inputX, inputY);

        if (context.getGuild().getSelfMember().hasPermission(context.getChannel(), Permission.MESSAGE_MANAGE))
            context.getMessage().delete().queue();
    }
}