package module4.homework.dao.repository

import io.getquill.{EntityQuery, Insert, Quoted}
import zio.Has
import io.getquill.context.ZioJdbc.QIO
import module4.homework.dao.entity.User
import zio.macros.accessible
import zio.{ULayer, ZLayer}
import module4.homework.dao.entity.{Role, UserToRole}
import module4.homework.dao.entity.UserId
import module4.homework.dao.entity.RoleCode
import module4.phoneBook.db


object UserRepository{


    val dc = db.Ctx
    import dc._

    type UserRepository = Has[Service]

    trait Service{
        def findUser(userId: UserId): QIO[Option[User]]
        def createUser(user: User): QIO[User]
        def createUsers(users: List[User]): QIO[List[User]]
        def updateUser(user: User): QIO[Unit]
        def deleteUser(user: User): QIO[Unit]
        def findByLastName(lastName: String): QIO[List[User]]
        def list(): QIO[List[User]]
        def userRoles(userId: UserId): QIO[List[Role]]
        def insertRoleToUser(roleCode: RoleCode, userId: UserId): QIO[Unit]
        def listUsersWithRole(roleCode: RoleCode): QIO[List[User]]
        def findRoleByCode(roleCode: RoleCode): QIO[Option[Role]]
    }

    class ServiceImpl extends Service{

        val userSchema = quote {
          querySchema[User](""""User"""")
        }

        val roleSchema = quote {
          querySchema[Role]("""Role""")
        }

        val userToRoleSchema = quote {
          querySchema[UserToRole]("""UserToRole""")
        }

        def findUser(userId: UserId): Result[Option[User]] = dc.run(
          userSchema.filter(u => u.id == lift(userId.id)).take(1)
        ).map(_.headOption)
        
        def createUser(user: User): Result[User] = dc.run(
          userSchema.insert(lift(user))
        )
        
        def createUsers(users: List[User]): Result[List[User]] = dc.run(
          liftQuery(users).foreach(e => userSchema.insert(e))
        )
        
        def updateUser(user: User): Result[Unit] = dc.run(
          userSchema.filter(_.id == lift(user.id)).update(lift(user))
        )
        
        def deleteUser(user: User): Result[Unit] = dc.run(
            userSchema.filter(_.id == lift(user.id)).delete
        )
        
        def findByLastName(lastName: String): Result[List[User]] = dc.run(
            userSchema.filter(u => u.lastName == lift(lastName))
        )
        
        def list(): Result[List[User]] = dc.run(userSchema)
        
        def userRoles(userId: UserId): Result[List[Role]] = dc.run(
          for {
            userToRole <- userToRoleSchema
            role <- roleSchema if lift(userId.id) == userToRole.userId
          } yield role
        )
        
        def insertRoleToUser(roleCode: RoleCode, userId: UserId): Result[Unit] = dc.run(
          userToRoleSchema.insert(UserToRole(lift(roleCode.code), lift(userId.id)))
        )
        
        def listUsersWithRole(roleCode: RoleCode): Result[List[User]] = dc.run(
          for {
            userToRole <- userToRoleSchema
            user <- userSchema if lift(roleCode.code) == userToRole.roleId && user.id == userToRole.userId
          } yield user
        )

        /*def listUsersWithRole(roleCode: RoleCode): Result[List[User]] = dc.run(
          for {
            user <- userSchema
            role <- roleSchema.join(_.code == userToRole.roleId)
          } yield (user, role)
        )*/
        
        def findRoleByCode(roleCode: RoleCode): Result[Option[Role]] = dc.run(
          roleSchema.filter(r => r.code == lift(roleCode.code)).take(1)
        ).map(_.headOption)
                
    }

    val live: ULayer[UserRepository] = ZLayer.succeed(new ServiceImpl)
}