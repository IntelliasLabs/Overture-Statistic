resource "aws_efs_file_system" "elasticsearch_data" {
  creation_token = "elasticsearch-data"
  encrypted      = true

  tags = {
    Name    = "overture-statistic-efs"
    Project = "overture-statistic"
  }
}

resource "aws_efs_mount_target" "public" {
  count           = length(var.public_subnets)
  file_system_id  = aws_efs_file_system.elasticsearch_data.id
  subnet_id       = aws_subnet.public[count.index].id
  security_groups = [aws_security_group.efs.id]
}

resource "aws_efs_access_point" "elasticsearch" {
  file_system_id = aws_efs_file_system.elasticsearch_data.id

  posix_user {
    gid = 1000
    uid = 1000
  }

  root_directory {
    path = "/elasticsearch"
    creation_info {
      owner_gid   = 1000
      owner_uid   = 1000
      permissions = "755"
    }
  }
}
