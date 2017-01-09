package io.lfgdiscordbot.core;

import io.lfgdiscordbot.Main;
import io.lfgdiscordbot.core.command.CommandHandler;
import io.lfgdiscordbot.utils.MessageUtilities;
import io.lfgdiscordbot.utils.__out;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.List;
import java.util.function.Consumer;

/**
 * Listens for new messages and performs actions during it's own
 * startup and join/leave guild events.
 */
public class EventListener extends ListenerAdapter
{
    // store the bot botSettings for easy reference
    private String prefix = Main.getBotSettings().getCommandPrefix();
    private String adminPrefix = Main.getBotSettings().getAdminPrefix();
    private String adminId = Main.getBotSettings().getAdminId();

    private static CommandHandler cmdHandler = Main.getCommandHandler();

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        // store some properties of the message for use later
        String content = event.getMessage().getContent();   // the raw string the user sent
        String userId = event.getAuthor().getId();          // the ID of the user

        if( content.startsWith(prefix) )
        {
            cmdHandler.handleCommand(event, 0);
            if( !event.getChannelType().equals(ChannelType.PRIVATE) )
                MessageUtilities.deleteMsg( event.getMessage(), null );
        }

        else if (content.startsWith(adminPrefix) && userId.equals(adminId))
        {
            cmdHandler.handleCommand(event, 1);
            if( !event.getChannelType().equals(ChannelType.PRIVATE) )
                MessageUtilities.deleteMsg( event.getMessage(), null );
        }

        else if(event.getChannel().getName().equals("lfg") && !event.getAuthor().getId().equals(Main.getBotSelfUser().getId()))
        {
            MessageUtilities.deleteMsg( event.getMessage(), null );
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event)
    {
        Guild guild = event.getGuild();

        Main.getGroupManager().addGuild( guild.getId() );

        if( !guild.getTextChannelsByName("lfg", false).isEmpty() )
        {
            TextChannel lfgChannel = guild.getTextChannelsByName("lfg", false).get(0);

            Consumer<List<Message>> clearChannel = (list) ->
            {
                for( Message message : list )
                {
                    MessageUtilities.deleteMsg( message, null );
                }
            };

            try
            {
                lfgChannel.getHistory().retrievePast(50).queue(clearChannel);
            }
            catch( Exception e )
            {
                __out.printOut(this.getClass(), "[" + guild.getId() + "] " + e.getMessage());
            }
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event)
    {
        Guild guild = event.getGuild();

        Main.getGroupManager().removeGuild( guild.getId() );
    }
}
