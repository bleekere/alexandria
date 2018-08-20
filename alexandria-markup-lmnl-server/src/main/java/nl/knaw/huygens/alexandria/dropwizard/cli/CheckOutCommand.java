package nl.knaw.huygens.alexandria.dropwizard.cli;

/*
 * #%L
 * alexandria-markup-lmnl-server
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
import nl.knaw.huygens.alexandria.lmnl.exporter.LMNLExporter;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.wrappers.DocumentWrapper;
import nl.knaw.huygens.alexandria.view.TAGView;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

public class CheckOutCommand extends AlexandriaCommand {
  private static final String VIEW = "view";
  private static final String DOCUMENT = "document";

  public CheckOutCommand() {
    super("checkout", "Check out a view on a document into a file for editing.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("-v", "--view")//
        .dest(VIEW)//
        .type(String.class)//
        .required(true)//
        .help("The name of the view to use");

    subparser.addArgument("-d", "--document")//
        .dest(DOCUMENT)//
        .type(String.class)//
        .required(true)//
        .help("The name of the document to view.");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    checkDirectoryIsInitialized();
    Map<String, Long> documentIndex = readDocumentIndex();
    Map<String, TAGView> viewMap = readViewMap();

    String viewName = namespace.getString(VIEW);
    String docName = namespace.getString(DOCUMENT);
    String outFilename = String.format("%s-%s.lmnl", docName, viewName);

    System.out.printf("Exporting document %s using view %s to %s...%n", docName, viewName, outFilename);
    Long docId = documentIndex.get(docName);
    store.open();
    store.runInTransaction(() -> {
      System.out.printf("Retrieving document %s%n", docName);
      TAGDocument document = store.getDocument(docId);
      DocumentWrapper documentWrapper = new DocumentWrapper(store, document);

      System.out.printf("Retrieving view %s%n", viewName);
      TAGView tagView = viewMap.get(viewName);

      System.out.printf("Exporting document view to %s%n", outFilename);
      LMNLExporter lmnlExporter = new LMNLExporter(store, tagView);
      String lmnl = lmnlExporter.toLMNL(documentWrapper)
          .replaceAll("\n\\s*\n", "\n")
          .trim();
      try {
        FileUtils.writeStringToFile(new File(outFilename), lmnl, Charsets.UTF_8);
        CLIContext context = readContext()//
            .setDocumentName(outFilename, docName)//
            .setViewName(outFilename, viewName);
        storeContext(context);
      } catch (IOException e) {
        e.printStackTrace();
        throw new UncheckedIOException(e);
      }
    });
    store.close();

    System.out.println("done!");
  }

}
