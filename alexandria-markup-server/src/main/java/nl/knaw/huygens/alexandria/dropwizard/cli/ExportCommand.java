package nl.knaw.huygens.alexandria.dropwizard.cli;

/*
 * #%L
 * alexandria-markup-server
 * =======
 * Copyright (C) 2015 - 2018 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.base.Charsets;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huc.di.tag.TAGViews;
import nl.knaw.huc.di.tag.model.graph.DotFactory;
import nl.knaw.huc.di.tag.tagml.xml.exporter.XMLExporter;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.view.TAGView;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class ExportCommand extends AlexandriaCommand {
  private static final String DOCUMENT = "document";
  private static final String FORMAT = "format";
  private static final String VIEW = "view";

  public ExportCommand() {
    super("export", "Export the document.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("-d", "--document")//
        .dest(DOCUMENT)//
        .type(String.class)//
        .required(true)//
        .help("The name of the document to export.");
    subparser.addArgument("-f", "--format")//
        .dest(FORMAT)
        .type(String.class)//
        .required(true)//
        .help("The format to export in. (currently supported: dot, svg, png, xml)");
    subparser.addArgument("-v", "--view")//
        .dest(VIEW)
        .type(String.class)//
        .required(false)//
        .help("The name of the view to use (only when format=xml)");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    checkDirectoryIsInitialized();
    Map<String, Long> documentIndex = readDocumentIndex();
    String docName = namespace.getString(DOCUMENT);
    String format = namespace.getString(FORMAT);
    String viewName = namespace.getString(VIEW);
    boolean useView = viewName != null;
    Long docId = documentIndex.get(docName);
    store.open();
    store.runInTransaction(() -> {
      System.out.printf("document: %s%n", docName);
      System.out.printf("format: %s%n", format);

      System.out.printf("Retrieving document %s%n", docName);
      TAGDocument document = store.getDocument(docId);

      TAGView tagView;
      if (useView) {
        Map<String, TAGView> viewMap = readViewMap();
        System.out.printf("Retrieving view %s%n", viewName);
        tagView = viewMap.get(viewName);
      } else {
        tagView = TAGViews.getShowAllMarkupView(store);
      }

      DotFactory dotFactory = new DotFactory();
      String dot = dotFactory.toDot(document, "");

      String sub = useView ? "-" + viewName : "";
      String fileName = String.format("%s%s.%s", docName, sub, format);
      System.out.printf("exporting to file %s...", fileName);
      try {
        switch (format) {
          case "xml":
            XMLExporter xmlExporter = new XMLExporter(store, tagView);
            String xml = xmlExporter.asXML(document);
            FileUtils.writeStringToFile(new File(fileName), xml, Charsets.UTF_8);
            break;

          case "dot":
            FileUtils.writeStringToFile(new File(fileName), dot, Charsets.UTF_8);
            break;

          case "png":
          case "svg":
            renderDotAs(dot, format, fileName);
            break;

          default:
            System.err.println("Unknown format: " + format);
            break;

        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      System.out.println();
      System.out.println("done!");

    });
    store.close();
  }

  private void renderDotAs(String dot, String format, String fileName) {
    DotEngine dotEngine = new DotEngine(Util.detectDotPath());
    File file = new File(fileName);
    try {
      file.createNewFile();
      FileOutputStream fos = new FileOutputStream(file);
      dotEngine.renderAs(format, dot, fos);
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}