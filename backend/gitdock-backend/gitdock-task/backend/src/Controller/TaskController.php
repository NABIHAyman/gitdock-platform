<?php

namespace App\Controller;

use App\Service\TaskService;
use App\DTOs\DTOtasks\TaskDto;
use App\Repository\EpicRepository;
use App\Repository\PartRepository;
use App\Repository\LevelRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\Routing\Annotation\Route;

final class TaskController extends AbstractController
{
    // ================= CREATE =================
    #[Route('/api/tasks', name: 'api_tasks_create', methods: ['POST'])]
    public function create(Request $request, TaskService $taskService): JsonResponse
    {
        $data = json_decode($request->getContent(), true);

        try {
            $dto = new TaskDto();
            $dto->title = $data['title'] ?? null;
            $dto->description = $data['description'] ?? null;
            $dto->status = $data['status'] ?? 'To Do';
            $dto->dueDate = $data['dueDate'] ?? null;
            $dto->assignedTo = $data['assignedTo'] ?? null;
            $dto->assignedBy = $data['assignedBy'] ?? null;
            $dto->epic = $data['epic'] ?? null;
            $dto->part = $data['part'] ?? null;
            $dto->level = $data['level'] ?? null;

            $task = $taskService->create($dto);

            return $this->json($this->formatTask($task), 201);

        } catch (\Throwable $e) {
            return $this->json(['error' => $e->getMessage()], 500);
        }
    }

    // ================= LIST =================
    #[Route('/api/tasks', name: 'api_tasks_list', methods: ['GET'])]
    public function list(TaskService $taskService): JsonResponse
    {
        $tasks = $taskService->getAll();
        return $this->json(array_map([$this, 'formatTaskList'], $tasks));
    }

    // ================= SHOW (IMPORTANT FIX 404) =================
    #[Route('/api/tasks/{id}', name: 'api_tasks_show', methods: ['GET'], requirements: ['id' => '\d+'])]
    public function show(int $id, TaskService $taskService): JsonResponse
    {
        try {
            $task = $taskService->getById($id);
            return $this->json($this->formatTask($task));
        } catch (\Throwable $e) {
            return $this->json([
                'error' => 'Task not found',
                'message' => $e->getMessage()
            ], 404);
        }
    }

    // ================= UPDATE =================
    #[Route('/api/tasks/{id}', name: 'api_tasks_update', methods: ['PUT'], requirements: ['id' => '\d+'])]
    public function update(int $id, Request $request, TaskService $taskService): JsonResponse
    {
        $data = json_decode($request->getContent(), true);

        try {
            $dto = new TaskDto();
            $dto->title = $data['title'] ?? null;
            $dto->description = $data['description'] ?? null;
            $dto->status = $data['status'] ?? null;
            $dto->dueDate = $data['dueDate'] ?? null;
            $dto->assignedTo = $data['assignedTo'] ?? null;
            $dto->assignedBy = $data['assignedBy'] ?? null;
            $dto->epic = $data['epic'] ?? null;
            $dto->part = $data['part'] ?? null;
            $dto->level = $data['level'] ?? null;

            $task = $taskService->update($id, $dto);

            return $this->json($this->formatTask($task));

        } catch (\Throwable $e) {
            return $this->json([
                'error' => 'Update failed',
                'message' => $e->getMessage()
            ], 404);
        }
    }

    // ================= DELETE =================
    #[Route('/api/tasks/{id}/soft-delete', name: 'api_tasks_soft_delete', methods: ['PUT'], requirements: ['id' => '\d+'])]
    public function softDelete(int $id, TaskService $taskService): JsonResponse
    {
        try {
            $task = $taskService->softDelete($id);

            return $this->json([
                'message' => 'Task deleted',
                'id' => $task->getId()
            ]);
        } catch (\Throwable $e) {
            return $this->json(['error' => $e->getMessage()], 404);
        }
    }

    // ================= DASHBOARD =================
    #[Route('/api/tasks/dashboard', name: 'api_tasks_dashboard', methods: ['GET'])]
    public function dashboard(TaskService $taskService): JsonResponse
    {
        return $this->json($taskService->getDashboardStats());
    }

    // ================= FORM DATA =================
        #[Route('/api/tasks/form-data', name: 'api_tasks_form_data', methods: ['GET'])]
        public function formData(
            Request $request, // <-- AJOUT DE LA REQUEST ICI
            EpicRepository $epicRepo,
            PartRepository $partRepo,
            LevelRepository $levelRepo,
            AuthServiceClient $authClient
        ): JsonResponse {

            // On attrape le token JWT envoyé par Vue.js
            $token = $request->headers->get('Authorization');

            return $this->json([
                'epics' => array_map(fn($e) => ['id' => $e->getId(), 'title' => $e->getTitle()], $epicRepo->findAll()),
                'parts' => array_map(fn($p) => ['id' => $p->getId(), 'name' => $p->getName()], $partRepo->findAll()),
                'levels' => array_map(fn($l) => ['id' => $l->getId(), 'name' => $l->getName()], $levelRepo->findAll()),

                // On transfère le token au service Auth
                'users' => $authClient->getAllUsersSummary($token),
            ]);
        }

    // ================= FORMAT SINGLE =================
    private function formatTask($task): array
    {
        return [
            'id' => $task->getId(),
            'title' => $task->getTitle(),
            'description' => $task->getDescription(),
            'status' => $task->getStatus(),
            'dueDate' => $task->getDueDate()?->format('Y-m-d H:i:s'),
            'assignedTo' => $task->getAssignedToUserId(),
            'assignedBy' => $task->getAssignedByUserId(),
            'epic' => $task->getEpic()?->getId(),
            'part' => $task->getPart()?->getId(),
            'level' => $task->getLevel()?->getId(),
        ];
    }

    // ================= FORMAT LIST =================
    private function formatTaskList($task): array
    {
        return [
            'id' => $task->getId(),
            'title' => $task->getTitle(),
            'status' => $task->getStatus(),
            'color' => match($task->getStatus()) {
                'To Do' => '#f97316',
                'In Progress' => '#3b82f6',
                'Done' => '#22c55e',
                default => '#6b7280'
            },
            'assignedTo' => $task->getAssignedToUserId(),
            'assignedBy' => $task->getAssignedByUserId(),
            'dueDate' => $task->getDueDate()?->format('Y-m-d H:i:s'),
        ];
    }
}
