package server;

import common.exception.AutorizacionException;import common.exception.ConflictoEdicionException;import common.exception.NoticiaNoEncontradaException;import common.model.*;import common.remote.TableroNoticiasRemote;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.io.TempDir;
import server.bootstrap.ServidorApplication;import server.config.ServerConfig;
import java.net.ServerSocket;import java.nio.file.Path;import java.rmi.registry.LocateRegistry;import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

class RmiIntegrationTest{
 @TempDir Path temp;
 @Test void flujoCrudViaStubRmi()throws Exception{int registryPort=freePort();ServerConfig config=new ServerConfig("127.0.0.1",registryPort,0,"TableroPrueba",temp.resolve("db/tablero"),Duration.ofMinutes(5));try(var server=ServidorApplication.start(config,false)){TableroNoticiasRemote stub=(TableroNoticiasRemote)LocateRegistry.getRegistry("127.0.0.1",registryPort).lookup("TableroPrueba");assertTrue(stub.verificarEstado().isBaseDatosDisponible());Sesion session=stub.iniciarSesion("redactor1","redactor123".toCharArray());Sesion other=stub.iniciarSesion("redactor2","noticias123".toCharArray());Noticia created=stub.publicarNoticia(session.getToken(),new NuevaNoticia("RMI real","Contenido remoto",Categoria.TECNOLOGIA));assertEquals(created.getId(),stub.obtenerNoticia(created.getId()).getId());assertThrows(AutorizacionException.class,()->stub.editarNoticia(other.getToken(),created.getId(),new ActualizacionNoticia("Ajena","Contenido",Categoria.CIENCIA),created.getVersion()));Noticia edited=stub.editarNoticia(session.getToken(),created.getId(),new ActualizacionNoticia("RMI editada","Contenido",Categoria.CIENCIA),created.getVersion());assertThrows(ConflictoEdicionException.class,()->stub.editarNoticia(session.getToken(),created.getId(),new ActualizacionNoticia("Vieja","Contenido",Categoria.CIENCIA),created.getVersion()));stub.eliminarNoticia(session.getToken(),edited.getId());assertThrows(NoticiaNoEncontradaException.class,()->stub.obtenerNoticia(edited.getId()));}}
 private int freePort()throws Exception{try(ServerSocket socket=new ServerSocket(0)){return socket.getLocalPort();}}
}
