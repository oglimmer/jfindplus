class Jfindplus < Formula
  desc "Find classes in JAR/EAR/WAR archives, detect duplicates, and diff jars"
  homepage "https://github.com/oglimmer/jfindplus"
  url "https://github.com/oglimmer/jfindplus/archive/refs/tags/v0.4.tar.gz"
  version "0.4"
  license "LGPL-2.1-or-later"

  head "https://github.com/oglimmer/jfindplus.git", branch: "master"

  depends_on "maven" => :build
  depends_on "openjdk@17"

  def install
    system "mvn", "-B", "-DskipTests", "package"
    libexec.install "target/jfindplus.jar"
    (bin/"jfindplus").write <<~EOS
      #!/bin/bash
      exec "#{Formula["openjdk@17"].opt_bin}/java" -jar "#{libexec}/jfindplus.jar" "$@"
    EOS
  end

  test do
    output = shell_output("#{bin}/jfindplus 2>&1", 1)
    assert_match "No command given", output
  end
end
