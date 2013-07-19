==========================================
PUMA - Pentaho Unified Management Archiver
===========================================
Unofficial application for file management over BiServer
using pentaho publish structure.
The goal is to implement a way to better manage pentaho files
as a developer and include images, css, js, xaction, ctools, etc.. over a single
application without the need of a file system access.

Problem:
=======
In some enviroments is not possible (or is trick and slow) to have direct access over the file system,
and even so some times publishing via ssh generate some permission issues.

Approach:
=======
Use (nearly) the same publishing structure alredy in place for the current pentaho tools... like
PRD, PME, SchemaWorkbench...
Use the serviceAction and other "http request" when needed.

Features:
=========
v0.5_alpha
- Publishing GUI;
- Read the remote solutions struture (dir) respecting pentaho permissions;
- Allow sending any files (xaction, cda, jpg, xml, ktr,kjb) throw pentaho publish;
- Allow sending multiple files in a single operation;
- Easy to use ssl certificate (*) - Add at java keystore [for https servers];
- Better controle over structures [for developers]:
  - hide/show files/folders(*);
  - rename over inde.xml(*) [meta naming the fisical folder];
  - delete hidden files(*);
  - download files(**);
- "auto refresh" repository over publishing(***);

(*) Under development;
(**) Under development and might need Ctools instaled;
(***) Under development and use xaction;

David <dguiarj at gmail dot com>

================
Libraries
===============
I will try to make it avalible at tgz lib pack...

commons-codec-1.8.jar               
commons-io-2.4.jar               
commons-lang3-3.1-javadoc.jar      
commons-logging-1.1.3-tests.jar         
pentaho-cwm-1.5.4.jar
commons-codec-1.8-javadoc.jar       
commons-io-2.4-javadoc.jar       
commons-lang3-3.1-sources.jar      
commons-logging-1.1.3-test-sources.jar  
pentaho-gwt-widgets.jar
commons-codec-1.8-sources.jar       
commons-io-2.4-sources.jar       
commons-lang3-3.1-tests.jar        
commons-logging-adapters-1.1.3.jar      
pentaho-metadata-3.3.1.jar
commons-codec-1.8-tests.jar         
commons-io-2.4-tests.jar         
commons-logging-1.1.3.jar          
commons-logging-api-1.1.3.jar           
swt.jar
commons-codec-1.8-test-sources.jar  
commons-io-2.4-test-sources.jar  
commons-logging-1.1.3-javadoc.jar  
commons-logging-tests.jar
commons-httpclient-3.0-rc4.jar      
commons-lang3-3.1.jar            
commons-logging-1.1.3-sources.jar  
org.eclipse.jface.jar

