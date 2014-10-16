package jenkins.plugins.hipchat;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

@SuppressWarnings({"unchecked"})
public class HipChatNotifier extends Notifier {

    private static final Logger logger = Logger.getLogger(HipChatNotifier.class.getName());

    private String authToken;
    private String buildServerUrl;
    private String room;
    private String sendAs;
    private boolean smartNotifications;
    private boolean startNotification;

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
    
    public String getConfiguredRoom() {
      if ( DESCRIPTOR.getRoom().equals(room) ) {
          return null;
      } else {
          return room;
      }
  }

    public String getConfiguredSendAs() {
      if ( DESCRIPTOR.getSendAs().equals(sendAs) ) {
          return null;
      } else {
          return sendAs;
      }
  }


    public void setBuildServerUrl(final String buildServerUrl) {
        this.buildServerUrl = buildServerUrl;
    }

    public void setAuthToken(final String authToken) {
        this.authToken = authToken;
    }

    public void setRoom(final String room) {
        this.room = room;
    }

    public void setSendAs(final String sendAs) {
        this.sendAs = sendAs;
    }

    @DataBoundConstructor
    public HipChatNotifier(final String authToken, final String room, String buildServerUrl, final String sendAs, final boolean smartNotifications, final boolean startNotification) {
        super();
        this.authToken = authToken;
        this.buildServerUrl = buildServerUrl;
        this.room = room;
        this.sendAs = sendAs;
        this.smartNotifications = smartNotifications;
        this.startNotification = startNotification;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    public HipChatService newHipChatService(final String room) {
        return new StandardHipChatService(getAuthToken(), room == null ? getRoom() : room, getSendAs() == null ? "Build Server" : getSendAs());
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return true;
    }

    /**
     * @return the smartNotifications
     */
    public boolean getSmartNotifications()
    {
      return smartNotifications;
    }

    /**
     * @param smartNotifications the smartNotifications to set
     */
    public void setSmartNotifications(boolean smartNotifications)
    {
      this.smartNotifications = smartNotifications;
    }

    /**
     * @return the startNotification
     */
    public boolean getStartNotification()
    {
      return startNotification;
    }

    /**
     * @param startNotification the startNotification to set
     */
    public void setStartNotification(boolean startNotification)
    {
      this.startNotification = startNotification;
    }

    /**
     * @return the authToken
     */
    public String getAuthToken()
    {
      return authToken;
    }

    /**
     * @return the buildServerUrl
     */
    public String getBuildServerUrl()
    {
      return buildServerUrl;
    }

    /**
     * @return the room
     */
    public String getRoom()
    {
      return room;
    }

    /**
     * @return the sendAs
     */
    public String getSendAs()
    {
      return sendAs;
    }
}
