package server.service;

import common.exception.*;
import common.model.*;
import common.validation.ValidadorNoticia;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;
import server.database.DatabaseManager;import server.repository.NoticiaRepository;
import java.sql.Connection;import java.sql.SQLException;import java.util.List;

public final class NoticiaService{
 private static final Logger LOGGER=LoggerFactory.getLogger(NoticiaService.class);private final DatabaseManager db;private final NoticiaRepository repo;private final SesionService sessions;
 public NoticiaService(DatabaseManager db,NoticiaRepository repo,SesionService sessions){this.db=db;this.repo=repo;this.sessions=sessions;}
 public List<Noticia> listar()throws ServicioNoDisponibleException{return read(c->repo.listar(c));}
 public List<Noticia> buscar(String text)throws ValidacionException,ServicioNoDisponibleException{String normalized=ValidadorNoticia.validarYNormalizarBusqueda(text);return read(c->repo.buscar(c,normalized));}
 public List<Noticia> categoria(Categoria category)throws ValidacionException,ServicioNoDisponibleException{if(category==null)throw new ValidacionException("La categoría es obligatoria.");return read(c->repo.porCategoria(c,category));}
 public Noticia obtener(long id)throws ValidacionException,NoticiaNoEncontradaException,ServicioNoDisponibleException{ValidadorNoticia.validarIdNoticia(id);try(Connection c=db.getConnection()){return repo.obtener(c,id).orElseThrow(()->new NoticiaNoEncontradaException("La noticia no existe."));}catch(SQLException e){throw unavailable("consultar",e);}}
 public Noticia publicar(String token,NuevaNoticia value)throws AutenticacionException,AutorizacionException,ValidacionException,ServicioNoDisponibleException{
  SesionServidor session=sessions.requerirRedactor(token);NuevaNoticia normalized=ValidadorNoticia.validarYNormalizar(value);
  try(Connection c=db.getConnection()){c.setAutoCommit(false);try{long id=repo.insertar(c,normalized,session.autorId());Noticia result=repo.obtener(c,id).orElseThrow(()->new SQLException("Inserción no recuperable"));c.commit();LOGGER.info("Noticia {} publicada por autor {}",id,session.autorId());return result;}catch(SQLException|RuntimeException e){rollback(c);throw e;}}
  catch(SQLException e){throw unavailable("publicar",e);}
 }
 public Noticia editar(String token,long id,ActualizacionNoticia value,int expected)throws AutenticacionException,AutorizacionException,ValidacionException,NoticiaNoEncontradaException,ConflictoEdicionException,ServicioNoDisponibleException{
  SesionServidor session=sessions.requerirRedactor(token);ValidadorNoticia.validarIdNoticia(id);ValidadorNoticia.validarVersion(expected);ActualizacionNoticia normalized=ValidadorNoticia.validarYNormalizar(value);
  try(Connection c=db.getConnection()){c.setAutoCommit(false);try{if(repo.actualizar(c,id,session.autorId(),expected,normalized)==0)diagnosticar(c,id,session.autorId(),expected);Noticia result=repo.obtener(c,id).orElseThrow(()->new NoticiaNoEncontradaException("La noticia no existe."));c.commit();LOGGER.info("Noticia {} editada por autor {}",id,session.autorId());return result;}catch(SQLException|RuntimeException|NoticiaNoEncontradaException|AutorizacionException|ConflictoEdicionException e){rollback(c);throw e;}}
  catch(SQLException e){throw unavailable("editar",e);}
 }
 public void eliminar(String token,long id)throws AutenticacionException,AutorizacionException,ValidacionException,NoticiaNoEncontradaException,ServicioNoDisponibleException{
  SesionServidor session=sessions.requerirRedactor(token);ValidadorNoticia.validarIdNoticia(id);
  try(Connection c=db.getConnection()){c.setAutoCommit(false);try{if(repo.eliminar(c,id,session.autorId())==0){var state=repo.estado(c,id);if(state.isEmpty())throw new NoticiaNoEncontradaException("La noticia no existe.");throw new AutorizacionException("Solo puede eliminar noticias propias.");}c.commit();LOGGER.info("Noticia {} eliminada por autor {}",id,session.autorId());}catch(SQLException|RuntimeException|NoticiaNoEncontradaException|AutorizacionException e){rollback(c);throw e;}}
  catch(SQLException e){throw unavailable("eliminar",e);}
 }
 private void diagnosticar(Connection c,long id,long author,int expected)throws SQLException,NoticiaNoEncontradaException,AutorizacionException,ConflictoEdicionException{var state=repo.estado(c,id).orElseThrow(()->new NoticiaNoEncontradaException("La noticia no existe."));if(state.autorId()!=author)throw new AutorizacionException("Solo puede editar noticias propias.");LOGGER.warn("Conflicto al editar noticia {}: esperada {}, actual {}",id,expected,state.version());throw new ConflictoEdicionException("La noticia fue modificada desde que se abrió. Actualice la información antes de volver a editarla.",expected,state.version());}
 private <T>T read(SqlRead<T> work)throws ServicioNoDisponibleException{try(Connection c=db.getConnection()){return work.apply(c);}catch(SQLException e){throw unavailable("consultar",e);}}
 private ServicioNoDisponibleException unavailable(String action,SQLException e){LOGGER.error("Error de base de datos al {} noticias",action,e);return new ServicioNoDisponibleException("El servicio de noticias no está disponible temporalmente.");}
 private static void rollback(Connection c){try{c.rollback();}catch(SQLException e){LOGGER.error("Falló el rollback",e);}}
 @FunctionalInterface private interface SqlRead<T>{T apply(Connection c)throws SQLException;}
}
