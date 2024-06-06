package com.pujh.router

import com.pujh.router.annotations.GenerateInterface

@GenerateInterface("UserRepository")
class MongoUserRepository<T> : UserRepository {

    override suspend fun findUser(userId: String): User? = TODO()

    override suspend fun findUsers(): List<User> = TODO()

    override suspend fun updateUser(user: User) {
        TODO()
    }

    override suspend fun insertUser(user: User) {
        TODO()
    }
}

//class FakeUserRepository : UserRepository {
//    private var users = listOf<User>()
//
//    override suspend fun findUser(userId: String): User? =
//        users.find { it.id == userId }
//
//    override suspend fun findUsers(): List<User> = users
//
//    override suspend fun updateUser(user: User) {
//        val oldUsers = users.filter { it.id == user.id }
//        users = users - oldUsers + user
//    }
//
//    override suspend fun insertUser(user: User) {
//        if (users.any { it.id == user.id }) {
//            throw DuplicatedUserId
//        }
//        users = users + user
//    }
//}