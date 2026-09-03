resource "aws_ecr_repository" "overture_batch" {
  name                 = "overture-converter-batch"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "overture-converter-batch"
  }
}

resource "aws_ecr_repository" "overture_extension" {
  name                 = "overture-framework-extension-app"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "overture-framework-extension-app"
  }
}

resource "aws_ecr_repository" "elasticsearch" {
  name                 = "elasticsearch-custom"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Name = "elasticsearch-custom"
  }
}
