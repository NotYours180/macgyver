package io.macgyver.core;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import io.macgyver.config.CoreConfig;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.runtime.metaclass.MissingMethodExceptionNoStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.StandardEnvironment;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

public class Kernel implements ApplicationContextAware {

	static Logger logger = LoggerFactory.getLogger(Kernel.class);

	static AtomicReference<Kernel> kernelRef = new AtomicReference<Kernel>();

	private ApplicationContext applicationContext;
	private File extensionDir;
	private static Throwable startupError = null;

	private Kernel(File extensionDir) {

		this.extensionDir = extensionDir.getAbsoluteFile();
		if (this.extensionDir.exists()) {
			try {
				extensionDir = extensionDir.getCanonicalFile();
			} catch (IOException e) {
				logger.warn(
						"could not determine canonical directory name for: {}",
						extensionDir);
			}
		}
	}

	public static Optional<Throwable> getStartupError() {
		return Optional.fromNullable(startupError);
	}

	public static void registerStartupError(Throwable t) {
		if (t != null) {
			startupError = t;
		}
	}

	public boolean isRunning() {
		return startupError == null;
	}

	public synchronized static void initialize() {
		if (kernelRef.get() == null) {

			Kernel k = new Kernel(Kernel.determineExtensionDir());
			kernelRef.set(k);
			AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
			

			k.applicationContext = ctx;

			File gbf = new File(Kernel.determineExtensionDir(),
					"conf/beans.groovy");
			if (gbf.exists()) {
				logger.info("loading spring java config from: {}", gbf);

				try {
					GroovyShell gs = new GroovyShell(Thread.currentThread()
							.getContextClassLoader());
					Object x = gs.evaluate(gbf);
					if (x==null || (! (x instanceof Class))) {
						throw new IllegalStateException(gbf+" must return a java.lang.Class");
					}
					ctx.setClassLoader((((Class) x).getClassLoader()));
					ctx.scan(CoreConfig.class.getPackage().getName());
					ctx.register((Class) x);
					ctx.refresh();

				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
				catch (MultipleCompilationErrorsException e) {
					throw e;
				}
				catch (RuntimeException e) {
					throw new IllegalStateException(gbf+" must return a java.lang.Class",e);
				}
			}
			kernelRef.set(k);

		} else {
			throw new IllegalStateException(
					"spring context already initialized");
		}
		if (startupError != null) {
			throw new MacGyverException(startupError);
		}
	}

	public synchronized static Kernel getInstance() {
		Kernel k = kernelRef.get();
		if (k == null) {
			Kernel.initialize();
			k = kernelRef.get();
			// throw new IllegalStateException("kernel not initialized");
		}
		return k;
	}

	public synchronized ApplicationContext getApplicationContext() {

		if (applicationContext == null) {

			throw new IllegalStateException(
					"Kernel's ApplicationContext not initialized");

		}
		return applicationContext;
	}

	public File getExtensionDir() {
		return extensionDir;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		if (this.applicationContext != null && this.applicationContext!=applicationContext) {

			throw new IllegalStateException("application context already set: "+this.applicationContext);
		}
		this.applicationContext = applicationContext;

	}

	static Optional<String> profile = null;

	public static synchronized Optional<String> getExecutionProfile() {
		if (profile!=null) {
			return profile;
		}
		StandardEnvironment standardEnvironment = new StandardEnvironment();
		
		String [] activeProfiles = standardEnvironment.getActiveProfiles();
		if (activeProfiles==null) {
			profile = Optional.absent();
		}
		for (String p: activeProfiles) {
			if (p!=null && p.endsWith("_env")) {
				p = p.substring(0,p.length()-"_env".length());
				profile = Optional.of(p);
			}
		}
		if (profile == null) {
			logger.info("no profile selected");
			profile=Optional.absent();
		}
		return profile;
		
	}

	public static File determineExtensionDir() {
		try {
			String location = System.getProperty("macgyver.ext.location");

			if (!Strings.isNullOrEmpty(location)) {
				return new File(location).getCanonicalFile();
			}
			File extLocation = new File("./src/test/resources/ext");
			if (extLocation.exists()) {
				return extLocation.getCanonicalFile();
			}
			return new File(".").getCanonicalFile();

		} catch (IOException e) {
			throw new ConfigurationException(e);
		}

	}
}
