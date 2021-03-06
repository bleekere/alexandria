package nl.knaw.huygens.alexandria.markup.client

/*
* #%L
 * alexandria-markup-client
 * =======
 * Copyright (C) 2015 - 2020 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * #L%
*/

import nl.knaw.huc.di.tag.tagml.exporter.TAGMLExporter
import nl.knaw.huc.di.tag.tagml.importer.TAGMLImporter
import nl.knaw.huygens.alexandria.dropwizard.ServerConfiguration
import nl.knaw.huygens.alexandria.dropwizard.api.DocumentService
import nl.knaw.huygens.alexandria.dropwizard.resources.AboutResource
import nl.knaw.huygens.alexandria.dropwizard.resources.DocumentsResource
import nl.knaw.huygens.alexandria.storage.BDBTAGStore
import nl.knaw.huygens.alexandria.texmecs.importer.TexMECSImporter
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.server.ResourceConfig
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter
import java.net.URI
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

@ClassRule
val tmpFolder = TemporaryFolder()

abstract class AlexandriaTestWithTestMarkupServer {
    companion object {
        private const val BASEURI = "http://localhost:2017/"
        protected val testURI: URI = URI.create(BASEURI)
        private var testServer: HttpServer? = null

        @BeforeClass
        @Throws(IOException::class)
        fun startTestServer() {
            val config = ServerConfiguration()
            config.baseURI = BASEURI
            config.store = BDBTAGStore(tmpFolder.newFolder("db").path, false)
            val resourceConfig = ResourceConfig()
            resourceConfig.register(AboutResource())
            val store = config.store
            resourceConfig.register(
                    DocumentsResource(
                            DocumentService(config),
                            TAGMLImporter(store),
                            TexMECSImporter(store),
                            TAGMLExporter(store),
                            config))
            testServer = GrizzlyHttpServerFactory.createHttpServer(testURI, resourceConfig, true)
        }

        @AfterClass
        fun stopTestServer() {
            testServer!!.shutdown()
        }

        @JvmOverloads
        fun prettyFormat(input: String?, indent: Int = 2): String {
            return try {
                val xmlInput: Source = StreamSource(StringReader(input))
                val stringWriter = StringWriter()
                val xmlOutput = StreamResult(stringWriter)
                val transformerFactory = TransformerFactory.newInstance()
                // transformerFactory.setAttribute("indent-number", indent);
                val transformer = transformerFactory.newTransformer()
                transformer.setOutputProperty(OutputKeys.INDENT, "yes")
                transformer.transform(xmlInput, xmlOutput)
                xmlOutput.writer.toString()
            } catch (e: Exception) {
                throw RuntimeException(e) // simple exception handling, please review it
            }
        }
    }
}
