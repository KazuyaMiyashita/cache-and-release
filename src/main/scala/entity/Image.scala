package entity

import java.util.UUID

case class ImageId(value: UUID)
case class Image(id: UUID, title: String, url: String)
