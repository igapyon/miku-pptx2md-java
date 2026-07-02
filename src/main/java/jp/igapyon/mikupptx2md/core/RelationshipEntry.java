package jp.igapyon.mikupptx2md.core;

final class RelationshipEntry {
  final String id;
  final String type;
  final String target;
  final String targetMode;

  RelationshipEntry(String id, String type, String target, String targetMode) {
    this.id = id;
    this.type = type;
    this.target = target;
    this.targetMode = targetMode;
  }
}
