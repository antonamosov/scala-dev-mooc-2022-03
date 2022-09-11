package module4.homework.services

import io.getquill.context.ZioJdbc.QIO
import zio.Has
import zio.Task
import module4.homework.dao.entity.User
import module4.homework.dao.entity.Role
import module4.homework.dao.repository.UserRepository
import zio.interop.catz._
import zio.ZIO
import zio.RIO
import module4.homework.dao.entity.UserToRole
import zio.ZLayer
import zio.macros.accessible
import module4.homework.dao.entity.RoleCode
import module4.phoneBook.db

@accessible
object UserService{
    type UserService = Has[Service]

    trait Service{
        def listUsers(): RIO[db.DataSource, List[User]]
        def listUsersDTO(): RIO[db.DataSource, List[UserDTO]]
        def addUserWithRole(user: User, roleCode: RoleCode): RIO[db.DataSource, UserDTO]
        def listUsersWithRole(roleCode: RoleCode): RIO[db.DataSource, List[UserDTO]]
    }

    class Impl(userRepo: UserRepository.Service) extends Service{
        val dc = db.Ctx
        import dc._

        def listUsers(): RIO[db.DataSource, List[User]] = userRepo.list()

        def listUsersDTO(): RIO[db.DataSource, List[UserDTO]] = ???
        
        def addUserWithRole(user: User, roleCode: RoleCode): RIO[db.DataSource, UserDTO] = for {
          user <- userRepo.createUser(user)
          _ <- userRepo.insertRoleToUser(roleCode, user.typedId)
          role <- userRepo.findRoleByCode(roleCode)
          dto <- ZIO.succeed(UserDTO(user, Set(role)))
        } yield dto
        
        def listUsersWithRole(roleCode: RoleCode): RIO[db.DataSource, List[UserDTO]] = for {
          users <- userRepo.listUsersWithRole(roleCode)
          role <- userRepo.findRoleByCode(roleCode)
          dto <- ZIO.succeed(
            users.map(user => UserDTO(user, Set(role)))
          )
        } yield dto
        
        
    }

    val live: ZLayer[UserRepository.UserRepository, Nothing, UserService] =
        ZLayer.fromServices[UserRepository.Service]((repo) => new Impl(repo))
}

case class UserDTO(user: User, roles: Set[Role])