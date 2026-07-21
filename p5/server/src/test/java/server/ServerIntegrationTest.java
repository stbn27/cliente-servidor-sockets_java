package server;

import common.exception.AutenticacionException;
import common.exception.AutorizacionException;
import common.exception.ConflictoEdicionException;
import common.model.*;
import org.junit.jupiter.api.*;
import server.database.*;import server.repository.*;import server.service.*;

import java.time.Duration;import java.util.List;import java.util.Set;import java.util.concurrent.*;import java.util.stream.Collectors;import java.util.stream.IntStream;
import java.sql.Connection;import java.sql.PreparedStatement;import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class ServerIntegrationTest {
 private DatabaseManager db;private AutenticacionService auth;private NoticiaService news;
 @BeforeEach void setup()throws Exception{db=DatabaseManager.inMemory("test_"+System.nanoTime());new DatabaseInitializer(db).initialize();var sessions=new SesionService(Duration.ofMinutes(30));auth=new AutenticacionService(db,new AutorRepository(),sessions);news=new NoticiaService(db,new NoticiaRepository(),sessions);}
 @AfterEach void close(){db.close();}
 @Test void autenticacionCrudBusquedaAutorizacionYVersion()throws Exception{
  assertThrows(AutenticacionException.class,()->auth.iniciar("redactor1","mal".toCharArray()));
  Sesion one=auth.iniciar("redactor1","redactor123".toCharArray()),two=auth.iniciar("redactor2","noticias123".toCharArray());
  Noticia created=news.publicar(one.getToken(),new NuevaNoticia("  Título distribuido  "," contenido concurrente ",Categoria.EDUCACION));assertEquals("Título distribuido",created.getTitulo());assertEquals(created.getId(),news.obtener(created.getId()).getId());assertFalse(news.buscar("DISTRIBUIDO").isEmpty());assertTrue(news.categoria(Categoria.EDUCACION).stream().anyMatch(n->n.getId()==created.getId()));
  assertThrows(AutorizacionException.class,()->news.editar(two.getToken(),created.getId(),new ActualizacionNoticia("Otro","Texto",Categoria.GENERAL),created.getVersion()));
  Noticia edited=news.editar(one.getToken(),created.getId(),new ActualizacionNoticia("Editada","Texto",Categoria.GENERAL),created.getVersion());assertEquals(created.getVersion()+1,edited.getVersion());assertThrows(ConflictoEdicionException.class,()->news.editar(one.getToken(),created.getId(),new ActualizacionNoticia("Antigua","Texto",Categoria.GENERAL),created.getVersion()));
  news.eliminar(one.getToken(),created.getId());assertThrows(common.exception.NoticiaNoEncontradaException.class,()->news.obtener(created.getId()));
 }
 @Test void publicacionesYLecturasConcurrentesSonConsistentes()throws Exception{
  Sesion session=auth.iniciar("redactor1","redactor123".toCharArray());int count=12;ExecutorService pool=Executors.newFixedThreadPool(8);CountDownLatch start=new CountDownLatch(1);
  try{List<Future<Noticia>> writes=IntStream.range(0,count).mapToObj(i->pool.submit(()->{start.await();return news.publicar(session.getToken(),new NuevaNoticia("Concurrente "+i,"Contenido "+i,Categoria.TECNOLOGIA));})).toList();List<Future<List<Noticia>>> reads=IntStream.range(0,12).mapToObj(i->pool.submit(()->{start.await();return news.listar();})).toList();start.countDown();Set<Long> ids=writes.stream().map(this::get).map(Noticia::getId).collect(Collectors.toSet());assertEquals(count,ids.size());for(Future<List<Noticia>> read:reads)assertNotNull(get(read));assertTrue(news.listar().size()>=count+3);}finally{pool.shutdownNow();}
 }
 @Test void soloUnaEdicionConcurrenteGana()throws Exception{
  Sesion session=auth.iniciar("redactor1","redactor123".toCharArray());Noticia base=news.publicar(session.getToken(),new NuevaNoticia("Base","Contenido",Categoria.CIENCIA));ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);Callable<Boolean> edit=()->{start.await();try{news.editar(session.getToken(),base.getId(),new ActualizacionNoticia("Cambio "+Thread.currentThread().getId(),"Contenido",Categoria.CIENCIA),base.getVersion());return true;}catch(ConflictoEdicionException e){return false;}};try{Future<Boolean>a=pool.submit(edit),b=pool.submit(edit);start.countDown();assertNotEquals(get(a),get(b));assertEquals(base.getVersion()+1,news.obtener(base.getId()).getVersion());}finally{pool.shutdownNow();}
 }
 @Test void rechazaUsuarioInexistenteInactivoTokenInvalidoYEliminacionAjena()throws Exception{
  assertThrows(AutenticacionException.class,()->auth.iniciar("nadie","secreto".toCharArray()));
  try(Connection c=db.getConnection();PreparedStatement ps=c.prepareStatement("UPDATE autores SET activo=FALSE WHERE usuario='redactor2'")){ps.executeUpdate();}
  assertThrows(AutenticacionException.class,()->auth.iniciar("redactor2","noticias123".toCharArray()));
  Sesion one=auth.iniciar("redactor1","redactor123".toCharArray());Noticia own=news.publicar(one.getToken(),new NuevaNoticia("Propia","Contenido",Categoria.GENERAL));
  assertThrows(AutenticacionException.class,()->news.publicar("token-invalido",new NuevaNoticia("X","Y",Categoria.GENERAL)));
  try(Connection c=db.getConnection();PreparedStatement ps=c.prepareStatement("UPDATE autores SET activo=TRUE WHERE usuario='redactor2'")){ps.executeUpdate();}
  Sesion two=auth.iniciar("redactor2","noticias123".toCharArray());assertThrows(AutorizacionException.class,()->news.eliminar(two.getToken(),own.getId()));
 }
 @Test void transaccionJdbcRevierteUnaEscrituraParcial()throws Exception{
  long before=news.listar().size();try(Connection c=db.getConnection()){c.setAutoCommit(false);try(PreparedStatement first=c.prepareStatement("INSERT INTO noticias(titulo,contenido,categoria,autor_id,version,fecha_creacion,fecha_modificacion) SELECT 'Temporal','Texto','GENERAL',id,1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP FROM autores WHERE usuario='redactor1'");PreparedStatement invalid=c.prepareStatement("INSERT INTO noticias(titulo,contenido,categoria,autor_id,version,fecha_creacion,fecha_modificacion) VALUES(NULL,'x','GENERAL',1,1,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)")){first.executeUpdate();assertThrows(SQLException.class,invalid::executeUpdate);c.rollback();}}
  assertEquals(before,news.listar().size());
 }
 private <T>T get(Future<T> future){try{return future.get(10,TimeUnit.SECONDS);}catch(Exception e){throw new AssertionError(e);}}
}
