package app.config

import pureconfig.ConfigReader

final case class PostgresConfig(nThreads: Int, url: String, user: String, pwd: String) derives ConfigReader
