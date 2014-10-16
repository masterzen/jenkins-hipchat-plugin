package jenkins.plugins.hipchat;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.tasks.Publisher;

import java.util.Map;
import java.util.logging.Logger;

@Extension
@SuppressWarnings("rawtypes")
public class HipChatListener extends RunListener<AbstractBuild> {

    private static final Logger logger = Logger.getLogger(HipChatListener.class.getName());

    public HipChatListener() {
        super(AbstractBuild.class);
    }

    @Override
    public void onCompleted(AbstractBuild r, TaskListener listener) {
        getNotifier(r.getProject()).completed(r);
        super.onCompleted(r, listener);
    }

    @Override
    public void onStarted(AbstractBuild r, TaskListener listener) {
        HipChatNotifier notifier = getPublisher(r.getProject());
        if (notifier != null && notifier.getStartNotification()) {
          getNotifier(r.getProject()).started(r);
        }
    }

    @Override
    public void onDeleted(AbstractBuild r) {
    }

    @Override
    public void onFinalized(AbstractBuild r) {
    }

    FineGrainedNotifier getNotifier(AbstractProject project) {
        HipChatNotifier notifier = getPublisher(project);
        if (notifier != null) {
          return new ActiveNotifier(notifier);
        }
        return new DisabledNotifier();
    }
    
    @SuppressWarnings("unchecked")
    HipChatNotifier getPublisher(AbstractProject project) {
      Map<Descriptor<Publisher>, Publisher> map = project.getPublishersList().toMap();
      for (Publisher publisher : map.values()) {
          if (publisher instanceof HipChatNotifier) {
            return (HipChatNotifier)publisher;
          }
      }
      return null;
    }

}
